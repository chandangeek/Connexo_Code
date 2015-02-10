package com.elster.jupiter.demo.impl.builders;

import java.util.Optional;

public interface Builder<T> {
    default T get(){
        return find().orElseGet(() -> create());
    }
    Optional<T> find();
    T create();
}
