package com.archiplugin.projectcreator.preferences;

import java.util.function.Consumer;
import java.util.function.Function;

public class Success<S, E> extends Result<S, E> {
	private S successResult;

	protected Success(S successResult) {
		this.successResult = successResult;
	}

	@Override
	public void onSuccessOrElse(Consumer<S> successHandler, Consumer<E> errorHandler) {
		successHandler.accept(successResult);
	}

	@Override
	public S orThrow(Function<E, RuntimeException> exceptionProducer) {
		return successResult;
	}
}
