package com.archiplugin.projectcreator.project;

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

	@Override
	public <T> Result<T, E> mapSuccess(Function<S, T> successMapper) {
		return new Failure<T, E>(errorResult);
	}

	@Override
	public <T> Result<T, E> lift(Function<S, Result<T, E>> successMapper) {
		return new Failure<T, E>(errorResult);
	}
	
	
	@Override
	public S recover(S failureValue) {
		return failureValue;
	}

	@Override
	public void onSuccess(Consumer<S> successHandler) {

	}
}
