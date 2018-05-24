package com.mybank.pc.collection.trade;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jfinal.aop.Duang;
import com.mybank.pc.collection.model.UnionpayCollection;

public class SyncExecutor {

	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

	private static CTradeSrv cCTradeSrv = Duang.duang(CTradeSrv.class);

	public static ScheduledFuture<UnionpayCollection> scheduleInProcessQuery(String orderId, long delay) {
		return schedule(new InProcessQuery(orderId, 0, delay), delay, TimeUnit.SECONDS);
	}

	public static ScheduledFuture<UnionpayCollection> scheduleInProcessQuery(String orderId, long delay,
			int maxAllowQueryCount) {
		return schedule(new InProcessQuery(orderId, 0, delay, maxAllowQueryCount), delay, TimeUnit.SECONDS);
	}

	public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
		return scheduledExecutorService.schedule(callable, delay, unit);
	}

	public static class InProcessQuery implements Callable<UnionpayCollection> {

		private int maxAllowQueryCount;
		private int preQueryCount;
		private long delay;
		private String orderId;

		public InProcessQuery(String orderId, int preQueryCount, long delay) {
			this.orderId = orderId;
			this.preQueryCount = preQueryCount;
			this.delay = delay;
			this.maxAllowQueryCount = 3;
		}

		public InProcessQuery(String orderId, int preQueryCount, long delay, int maxAllowQueryCount) {
			this(orderId, preQueryCount, delay);
			this.maxAllowQueryCount = maxAllowQueryCount;
		}

		@Override
		public UnionpayCollection call() {
			UnionpayCollection unionpayCollection = UnionpayCollection.findByOrderId(orderId);
			if (allowSync(unionpayCollection)) {
				try {
					cCTradeSrv.syncOrderStatus(unionpayCollection);
				} catch (Exception e) {
					e.printStackTrace();
				}
				unionpayCollection = UnionpayCollection.findByOrderId(orderId);
				if (allowSync(unionpayCollection)) {
					int currentQueryCount = preQueryCount + 1;
					Integer queryResultCount = unionpayCollection.getQueryResultCount();
					if (currentQueryCount > maxAllowQueryCount
							|| (queryResultCount != null && queryResultCount > maxAllowQueryCount)) {
						return unionpayCollection;
					}
					long currentDelay = delay * 2;
					InProcessQuery nextTask = new InProcessQuery(orderId, currentQueryCount, currentDelay);
					SyncExecutor.schedule(nextTask, currentDelay, TimeUnit.SECONDS);
				}
			}
			return unionpayCollection;
		}

		private static boolean allowSync(UnionpayCollection unionpayCollection) {
			if (unionpayCollection == null) {
				return false;
			}
			String respCode = unionpayCollection.getRespCode();
			String finalCode = unionpayCollection.getFinalCode();
			return "00".equals(respCode) && "1".equals(finalCode);
		}

	}

}
