package com.archiplugin.projectcreator.preferences;

import java.util.function.Consumer;
import java.util.function.Function;

abstract class Result<S, E> {

	public static <S, E> Result<S, E> succeededWith(S successResult) {
		return new Success<>(successResult);
	}

	public static <S, E> Result<S, E> failedWith(E errorResult) {
		return new Failure<>(errorResult);
	}

	public abstract void onSuccessOrElse(Consumer<S> successHandler, Consumer<E> errorHandler);

	public abstract S orThrow(Function<E, RuntimeException> exceptionProducer);
}
