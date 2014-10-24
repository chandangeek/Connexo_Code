package com.elster.jupiter.devtools.tests.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.assertj.core.error.ErrorMessageFactory;
import org.assertj.core.internal.Failures;
import org.assertj.core.internal.Objects;
import org.assertj.core.util.VisibleForTesting;

import java.util.Optional;

public final class OptionalAssert<T> extends AbstractAssert<OptionalAssert<T>, Optional<T>> {

    @VisibleForTesting
    Failures failures = Failures.instance();

    protected OptionalAssert(final Optional<T> actual) {
        super(actual, OptionalAssert.class);
    }

    // visible for test
    protected Optional<T> getActual() {
        return actual;
    }

    /**
     * Verifies that the actual {@link Optional} contains the given value.<br>
     * <p>
     * Example :
     *
     * <pre>
     * Optional&lt;String&gt; optional = Optional.of(&quot;Test&quot;);
     * assertThat(optional).hasValue(&quot;Test&quot;);
     * </pre>
     *
     * @param value the value to look for in actual {@link Optional}.
     * @return this {@link OptionalAssert} for assertions chaining.
     *
     * @throws AssertionError if the actual {@link Optional} is {@code null}.
     * @throws AssertionError if the actual {@link Optional} contains nothing or does not have the given value.
     */
    public OptionalAssert<T> contains(final Object value) {
        Objects.instance().assertNotNull(info, actual);
        if (!actual.isPresent()) {
            throw failures.failure(info, shouldBePresentWithValue(value));
        }
        if (!actual.get().equals(value)) {
            throw failures.failure(info, shouldBePresentWithValue(actual, value));
        }
        return this;
    }

    /**
     * Verifies that the actual {@link Optional} contained instance is absent/null (ie. not {@link Optional#isPresent()}).<br>
     * <p>
     * Example :
     *
     * <pre>
     * Optional&lt;String&gt; optional = Optional.absent();
     * assertThat(optional).isAbsent();
     * </pre>
     *
     * @return this {@link OptionalAssert} for assertions chaining.
     *
     * @throws AssertionError if the actual {@link Optional} is {@code null}.
     * @throws AssertionError if the actual {@link Optional} contains a (non-null) instance.
     */
    public OptionalAssert<T> isAbsent() {
        Objects.instance().assertNotNull(info, actual);
        if (actual.isPresent()) {
            throw failures.failure(info, shouldBeAbsent(actual));
        }
        return this;
    }

    /**
     * Verifies that the actual {@link Optional} contains a (non-null) instance.<br>
     * <p>
     * Example :
     *
     * <pre>
     * Optional&lt;String&gt; optional = Optional.of(&quot;value&quot;);
     * assertThat(optional).isPresent();
     * </pre>
     *
     * @return this {@link OptionalAssert} for assertions chaining.
     *
     * @throws AssertionError if the actual {@link Optional} is {@code null}.
     * @throws AssertionError if the actual {@link Optional} contains a null instance.
     */
    public OptionalAssert<T> isPresent() {
        Objects.instance().assertNotNull(info, actual);
        if (!actual.isPresent()) {
            throw failures.failure(info, shouldBePresent(actual));
        }
        return this;
    }

    private static <T> ErrorMessageFactory shouldBeAbsent(final Optional<T> actual) {
        return new OptionalShouldBeAbsent("Expecting Optional to contain nothing (absent Optional) but contained <%s>",
                actual.get());
    }

    private static <T> ErrorMessageFactory shouldBePresent(final Optional<T> actual) {
        return new OptionalShouldBePresent(
                "Expecting Optional to contain a non-null instance but contained nothing (absent Optional)", actual);
    }

    public static <T> ErrorMessageFactory shouldBePresentWithValue(final Optional<T> actual, final Object value) {
        return new OptionalShouldBePresentWithValue("\nExpecting Optional to contain value \n<%s>\n but contained \n<%s>",
                value, actual.get());
    }

    public static <T> ErrorMessageFactory shouldBePresentWithValue(final Object value) {
        return new OptionalShouldBePresentWithValue(
                "Expecting Optional to contain <%s> but contained nothing (absent Optional)", value);
    }


    private static final class OptionalShouldBeAbsent extends BasicErrorMessageFactory {

        private OptionalShouldBeAbsent(final String format, final Object... arguments) {
            super(format, arguments);
        }

    }

    private static final class OptionalShouldBePresent extends BasicErrorMessageFactory {

        private OptionalShouldBePresent(final String format, final Object... arguments) {
            super(format, arguments);
        }

    }

    private static final class OptionalShouldBePresentWithValue extends BasicErrorMessageFactory {

        private OptionalShouldBePresentWithValue(final String format, final Object... arguments) {
            super(format, arguments);
        }

    }

}
