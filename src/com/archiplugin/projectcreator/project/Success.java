package com.archiplugin.projectcreator.project;

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

	@Override
	public <T> Result<T, E> mapSuccess(Function<S, T> successMapper) {
		return new Success<>(successMapper.apply(successResult));
	}

	@Override
	public <T> Result<T, E> lift(Function<S, Result<T, E>> successMapper) {
		return successMapper.apply(successResult);
	}

	@Override
	public S recover(S failureValue) {
		return successResult;
	}

	@Override
	public void onSuccess(Consumer<S> successHandler) {
		successHandler.accept(successResult);

	}
}
