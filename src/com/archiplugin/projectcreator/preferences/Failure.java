package com.archiplugin.projectcreator.preferences;

import java.util.function.Consumer;
import java.util.function.Function;

public class Failure<S, E> extends Result<S, E> {
	private E errorResult;

	protected Failure(E errorResult) {
		this.errorResult = errorResult;
	}

	@Override
	public void onSuccessOrElse(Consumer<S> successHandler, Consumer<E> errorHandler) {
		errorHandler.accept(errorResult);
	}

	@Override
	public S orThrow(Function<E, RuntimeException> exceptionProducer) {
		throw exceptionProducer.apply(errorResult);
	}
}
