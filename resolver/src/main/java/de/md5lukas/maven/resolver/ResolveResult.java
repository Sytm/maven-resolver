package de.md5lukas.maven.resolver;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString(doNotUseGetters = true)
public final class ResolveResult<T> {

    private static final ResolveResult<?> NOT_FOUND = new ResolveResult<>(ResolveStatus.NOT_FOUND, null, null);

    @SuppressWarnings("unchecked")
    public static <T> ResolveResult<T> notFound() {
        return (ResolveResult<T>) NOT_FOUND;
    }

    public static <T> ResolveResult<T> success(@NotNull @NonNull T value) {
        return new ResolveResult<>(ResolveStatus.SUCCESS, value, null);
    }

    public static <T> ResolveResult<T> error(@NotNull @NonNull Exception exception) {
        return new ResolveResult<>(ResolveStatus.ERROR, null, exception);
    }

    @SuppressWarnings("unchecked")
    public <U> ResolveResult<U> castError() {
        if (status != ResolveStatus.ERROR)
            throw new IllegalStateException("Can only cast a resolve result if it is an error");
        return (ResolveResult<U>) this;
    }

    @NotNull
    private final ResolveStatus status;
    @Nullable
    private final T value;
    @Nullable
    private final Exception exception;

    @NotNull
    public T getValue() {
        if (value == null)
            throw new IllegalStateException("Can only get a value if the the result is successful");
        return value;
    }

    @NotNull
    public Exception getException() {
        if (exception == null)
            throw new IllegalStateException("Can only get an exception if the the result is an error");
        return exception;
    }
}