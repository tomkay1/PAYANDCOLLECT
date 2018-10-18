package com.mybank.pc.interceptors;

import com.jfinal.aop.Duang;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.mybank.pc.advance.trade.ATradeSrv;
import com.mybank.pc.exception.AdvanceRuntimeException;

public class AdvanceExceptionInterceptor implements Interceptor {

	private ATradeSrv aTradeSrv = Duang.duang(ATradeSrv.class);

	public void intercept(Invocation invocation) {
		try {
			invocation.invoke();
		} catch (AdvanceRuntimeException e) {
			aTradeSrv.handlingException(invocation, e);
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

}
