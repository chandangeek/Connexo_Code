package com.elster.jupiter.transaction;

@FunctionalInterface
public interface Transaction<T> {

    T perform();

}
