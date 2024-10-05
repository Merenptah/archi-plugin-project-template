package com.archiplugin.projectcreator.project;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

abstract public class Result<S, E> {

	public static <S, E> Result<S, E> succeededWith(S successResult) {
		return new Success<>(successResult);
	}

	public static <S, E> Result<S, E> failedWith(E errorResult) {
		return new Failure<>(errorResult);
	}

	public static <S, E> Result<S, E> fromOptional(Optional<S> opt, E errorMessage) {
		return opt.map(o -> Result.<S, E>succeededWith(o)).orElse(Result.failedWith(errorMessage));
	}

	public abstract void onSuccessOrElse(Consumer<S> successHandler, Consumer<E> errorHandler);

	public abstract void onSuccess(Consumer<S> successHandler);

	public abstract <T> Result<T, E> mapSuccess(Function<S, T> successMapper);

	public abstract S recover(S failureValue);

	public abstract <T> Result<T, E> lift(Function<S, Result<T, E>> successMapper);

	public abstract S orThrow(Function<E, RuntimeException> exceptionProducer);
}
