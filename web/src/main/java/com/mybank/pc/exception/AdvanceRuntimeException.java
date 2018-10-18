package com.mybank.pc.exception;

import com.mybank.pc.advance.model.UnionpayAdvance;

public class AdvanceRuntimeException extends BaseAdvanceRuntimeException {

	private static final long serialVersionUID = 1L;

	private UnionpayAdvance unionpayAdvance;

	public AdvanceRuntimeException() {
		super();
	}

	public AdvanceRuntimeException(String message) {
		super(message);
	}

	public AdvanceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AdvanceRuntimeException(Throwable cause) {
		super(cause);
	}

	protected AdvanceRuntimeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnionpayAdvance getUnionpayAdvance() {
		return unionpayAdvance;
	}

	public void setUnionpayAdvance(UnionpayAdvance unionpayAdvance) {
		this.unionpayAdvance = unionpayAdvance;
	}

}
