/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.concurrent;

import com.elster.jupiter.util.HasId;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class LockUtils {
    private LockUtils() {
        // prevent from instantiation
    }

    /**
     *
     * @param object
     * @param lockFunction
     * @param check
     * @param exceptionSupplier The supplier for exception in case the object can't be locked (should treat {@code null} for this case!) or fails the check.
     * @param <T>
     * @param <E>
     * @return The locked object
     * @throws E
     */
    public static <T extends HasId, E extends Exception> T forceLockWithDoubleCheck(T object,
                                                                                    Function<Long, Optional<T>> lockFunction,
                                                                                    Predicate<? super T> check,
                                                                                    Function<? super T, E> exceptionSupplier) throws E {
        return forceLockWithDoubleCheck(object, lockFunction, check, check, exceptionSupplier);
    }

    /**
     *
     * @param object
     * @param lockFunction
     * @param checkBefore
     * @param checkAfter
     * @param exceptionSupplier The supplier for exception in case the object can't be locked (should treat {@code null} for this case!) or fails one of the checks.
     * @param <T>
     * @param <E>
     * @return The locked object
     * @throws E
     */
    public static <T extends HasId, E extends Exception> T forceLockWithDoubleCheck(T object,
                                                                                    Function<Long, Optional<T>> lockFunction,
                                                                                    Predicate<? super T> checkBefore,
                                                                                    Predicate<? super T> checkAfter,
                                                                                    Function<? super T, E> exceptionSupplier) throws E {
        T unlocked = Optional.ofNullable(object)
                .filter(checkBefore)
                .orElseThrow(() -> exceptionSupplier.apply(object));
        T locked = lockFunction.apply(unlocked.getId())
                .orElseThrow(() -> exceptionSupplier.apply(null));
        if (!checkAfter.test(locked)) {
            throw exceptionSupplier.apply(locked);
        }
        return locked;
    }

    public static <T extends HasId> Optional<T> lockWithDoubleCheck(T object,
                                                                    Function<Long, Optional<T>> lockFunction,
                                                                    Predicate<? super T> check) {
        return lockWithDoubleCheck(object, lockFunction, check, check);
    }

    public static <T extends HasId> Optional<T> lockWithDoubleCheck(T object,
                                                                    Function<Long, Optional<T>> lockFunction,
                                                                    Predicate<? super T> checkBefore,
                                                                    Predicate<? super T> checkAfter) {
        return Optional.ofNullable(object)
                .filter(checkBefore)
                .map(HasId::getId)
                .flatMap(lockFunction)
                .filter(checkAfter);
    }

    public static <T extends HasId> Optional<T> lockWithPostCheck(T object,
                                                                    Function<Long, Optional<T>> lockFunction,
                                                                    Predicate<? super T> check) {
        return Optional.ofNullable(object)
                .map(HasId::getId)
                .flatMap(lockFunction)
                .filter(check);
    }
}
