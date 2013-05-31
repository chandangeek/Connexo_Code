package com.elster.jupiter.transaction;


public interface Transaction<T> {

    T perform();

}
