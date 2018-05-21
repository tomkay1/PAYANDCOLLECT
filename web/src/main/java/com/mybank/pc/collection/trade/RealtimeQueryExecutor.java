package com.mybank.pc.collection.trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.jfinal.aop.Duang;
import com.mybank.pc.collection.model.UnionpayCollection;

public class RealtimeQueryExecutor {

	private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

	private static CTradeSrv cCTradeSrv = Duang.duang(CTradeSrv.class);

	public static <T> ScheduledFuture<T> schedule(Callable<T> callable, long delay, TimeUnit unit) {
		return scheduledExecutorService.schedule(callable, delay, unit);
	}

	public static class InProcessQuery implements Callable<UnionpayCollection> {

		private String orderId;

		public InProcessQuery(String orderId) {
			this.orderId = orderId;
		}

		@Override
		public UnionpayCollection call() throws Exception {
			UnionpayCollection unionpayCollection = UnionpayCollection.findByOrderId(orderId);
			if (unionpayCollection == null) {
				return null;
			}
			String respCode = unionpayCollection.getRespCode();
			String finalCode = unionpayCollection.getFinalCode();
			Integer queryResultCount = unionpayCollection.getQueryResultCount();
			if ((("00").equals(respCode) || "A6".equals(respCode) || ("03").equals(respCode) || ("04").equals(respCode)
					|| ("05").equals(respCode)) && "1".equals(finalCode)) {
				
				cCTradeSrv.syncOrderStatus(unionpayCollection);
			}


			return unionpayCollection;
		}

	}

	public static void main(String[] args) {

		TestCallable tc = new TestCallable(0);
		ScheduledFuture<String> sf = scheduledExecutorService.schedule(tc, 3, TimeUnit.SECONDS);
		// try {
		// String r = sf.get();
		// System.out.println(r);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// } catch (ExecutionException e) {
		// e.printStackTrace();
		// }
		System.out.println("ttttteeeeessssstttttt");
	}

}

class TestCallable implements Callable<String> {

	private int count;

	public TestCallable(int count) {
		this.count = count;
	}

	@Override
	public String call() throws Exception {
		String result = "count " + count + "test" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		System.out.println(result);

		if (count < 3) {
			TestCallable tc = new TestCallable(count + 1);
			ScheduledFuture<String> sf = RealtimeQueryExecutor.schedule(tc, 1, TimeUnit.SECONDS);
		}

		return "result" + result;
	}

}
