package com.mybank.pc.advance.trade;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jfinal.aop.Duang;
import com.mybank.pc.advance.model.UnionpayAdvance;

public class AdvanceSyncStatusExecutor {

	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

	private static ATradeSrv aTradeSrv = Duang.duang(ATradeSrv.class);

	public static ScheduledFuture<UnionpayAdvance> scheduleInProcessSingleQuery(String orderId, long delay,
			TimeUnit unit) {
		return schedule(new InProcessSingleQuery(orderId, 0, delay), delay, unit);
	}

	public static ScheduledFuture<UnionpayAdvance> scheduleInProcessSingleQuery(String orderId, long delay,
			int maxAllowQueryCount, TimeUnit unit) {
		return schedule(new InProcessSingleQuery(orderId, 0, delay, maxAllowQueryCount), delay, unit);
	}

	public static ScheduledFuture<UnionpayAdvance> scheduleNotClearSingleQuery(String orderId, long delay,
			TimeUnit unit) {
		return schedule(new NotClearSingleQuery(orderId), delay, unit);
	}

	public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
		return scheduledExecutorService.schedule(callable, delay, unit);
	}

	public static class InProcessSingleQuery implements Callable<UnionpayAdvance> {

		private int maxAllowQueryCount;
		private int preQueryCount;
		private long delay;
		private String orderId;

		public InProcessSingleQuery(String orderId, int preQueryCount, long delay) {
			this.orderId = orderId;
			this.preQueryCount = preQueryCount;
			this.delay = delay;
			this.maxAllowQueryCount = 3;
		}

		public InProcessSingleQuery(String orderId, int preQueryCount, long delay, int maxAllowQueryCount) {
			this(orderId, preQueryCount, delay);
			this.maxAllowQueryCount = maxAllowQueryCount;
		}

		@Override
		public UnionpayAdvance call() {
			UnionpayAdvance unionpayAdvance = UnionpayAdvance.findByOrderId(orderId);
			if (allowSync(unionpayAdvance)) {
				try {
					aTradeSrv.syncOrderStatus(unionpayAdvance);
				} catch (Exception e) {
					e.printStackTrace();
				}
				unionpayAdvance = UnionpayAdvance.findByOrderId(orderId);
				if (allowSync(unionpayAdvance)) {
					int currentQueryCount = preQueryCount + 1;
					Integer queryResultCount = unionpayAdvance.getQueryResultCount();
					if (currentQueryCount > maxAllowQueryCount
							|| (queryResultCount != null && queryResultCount > maxAllowQueryCount)) {
						return unionpayAdvance;
					}
					long currentDelay = delay == 0 ? 5 : delay * 2;
					InProcessSingleQuery nextTask = new InProcessSingleQuery(orderId, currentQueryCount, currentDelay);
					AdvanceSyncStatusExecutor.schedule(nextTask, currentDelay, TimeUnit.SECONDS);
				}
			}
			return unionpayAdvance;
		}

		private static boolean allowSync(UnionpayAdvance unionpayAdvance) {
			if (unionpayAdvance == null) {
				return false;
			}
			String respCode = unionpayAdvance.getRespCode();
			String finalCode = unionpayAdvance.getFinalCode();
			return "00".equals(respCode) && "1".equals(finalCode);
		}

	}

	public static class NotClearSingleQuery implements Callable<UnionpayAdvance> {

		private static int maxAllowQueryCount = 2;

		private int preQueryCount;
		private String orderId;

		public NotClearSingleQuery(String orderId) {
			this.orderId = orderId;
			this.preQueryCount = 0;
		}

		private NotClearSingleQuery(String orderId, int preQueryCount) {
			this.orderId = orderId;
			this.preQueryCount = preQueryCount;
		}

		@Override
		public UnionpayAdvance call() {
			UnionpayAdvance unionpayAdvance = UnionpayAdvance.findByOrderId(orderId);
			if (allowSync(unionpayAdvance)) {
				try {
					aTradeSrv.syncOrderStatus(unionpayAdvance);
				} catch (Exception e) {
					e.printStackTrace();
				}
				unionpayAdvance = UnionpayAdvance.findByOrderId(orderId);
				if (allowSync(unionpayAdvance)) {
					int currentQueryCount = preQueryCount + 1;
					if (currentQueryCount == maxAllowQueryCount) {
						return unionpayAdvance;
					}

					long currentDelay = UnionpayAdvance.TIMEOUT_MINUTE;
					NotClearSingleQuery nextTask = new NotClearSingleQuery(orderId, currentQueryCount);
					AdvanceSyncStatusExecutor.schedule(nextTask, currentDelay, TimeUnit.MINUTES);
				}
			}
			return unionpayAdvance;
		}

		private static boolean allowSync(UnionpayAdvance unionpayAdvance) {
			if (unionpayAdvance == null) {
				return false;
			}
			return "3".equals(unionpayAdvance.getFinalCode());
		}

	}

}
