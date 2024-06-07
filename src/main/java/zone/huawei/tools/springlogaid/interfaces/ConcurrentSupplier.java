package zone.huawei.tools.springlogaid.interfaces;

import zone.huawei.tools.springlogaid.exception.SubThreadException;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@FunctionalInterface
public interface ConcurrentSupplier<T> extends Supplier<T> {

    /**
     * Gets a result, possibly throwing a checked exception.
     *
     * @return a result
     * @throws Exception on error
     */
    T getWithException() throws Exception;

    /**
     * Default {@link Supplier#get()} that wraps any thrown checked exceptions
     * (by default in a {@link SubThreadException}).
     *
     * @see Supplier#get()
     */
    @Override
    default T get() {
        return get(SubThreadException::new);
    }

    /**
     * Gets a result, wrapping any thrown checked exceptions using the given
     * {@code exceptionWrapper}.
     *
     * @param exceptionWrapper {@link BiFunction} that wraps the given message
     *                         and checked exception into a runtime exception
     * @return a result
     */
    default T get(BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
        try {
            return getWithException();
        } catch (SubThreadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionWrapper.apply(ex.getMessage(), ex);
        }
    }

    /**
     * Return a new {@link ConcurrentSupplier} where the {@link #get()} method
     * wraps any thrown checked exceptions using the given
     * {@code exceptionWrapper}.
     *
     * @param exceptionWrapper {@link BiFunction} that wraps the given message
     *                         and checked exception into a runtime exception
     * @return the replacement {@link ConcurrentSupplier} instance
     */
    default ConcurrentSupplier<T> throwing(BiFunction<String, Exception, RuntimeException> exceptionWrapper) {
        return new ConcurrentSupplier<>() {
            @Override
            public T getWithException() throws Exception {
                return ConcurrentSupplier.this.getWithException();
            }

            @Override
            public T get() {
                return get(exceptionWrapper);
            }
        };
    }

    /**
     * Lambda friendly convenience method that can be used to create a
     * {@link ConcurrentSupplier} where the {@link #get()} method wraps any checked
     * exception thrown by the supplied lambda expression or method reference.
     * <p>This method can be especially useful when working with method references.
     * It allows you to easily convert a method that throws a checked exception
     * into an instance compatible with a regular {@link Supplier}.
     * <p>For example:
     * <pre class="code">
     * optional.orElseGet(ConcurrentSupplier.of(Example::methodThatCanThrowCheckedException));
     * </pre>
     *
     * @param <T>      the type of results supplied by this supplier
     * @param supplier the source supplier
     * @return a new {@link ConcurrentSupplier} instance
     */
    static <T> ConcurrentSupplier<T> of(ConcurrentSupplier<T> supplier) {
        return supplier;
    }

    /**
     * Lambda friendly convenience method that can be used to create
     * {@link ConcurrentSupplier} where the {@link #get()} method wraps any
     * thrown checked exceptions using the given {@code exceptionWrapper}.
     * <p>This method can be especially useful when working with method references.
     * It allows you to easily convert a method that throws a checked exception
     * into an instance compatible with a regular {@link Supplier}.
     * <p>For example:
     * <pre class="code">
     * optional.orElseGet(ConcurrentSupplier.of(Example::methodThatCanThrowCheckedException, IllegalStateException::new));
     * </pre>
     *
     * @param <T>              the type of results supplied by this supplier
     * @param supplier         the source supplier
     * @param exceptionWrapper the exception wrapper to use
     * @return a new {@link ConcurrentSupplier} instance
     */
    static <T> ConcurrentSupplier<T> of(ConcurrentSupplier<T> supplier,
                                        BiFunction<String, Exception, RuntimeException> exceptionWrapper) {

        return supplier.throwing(exceptionWrapper);
    }
}
