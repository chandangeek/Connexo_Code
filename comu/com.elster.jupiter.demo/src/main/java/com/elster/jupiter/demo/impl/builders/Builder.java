package com.elster.jupiter.demo.impl.builders;

import java.util.Optional;

public interface Builder<T> {
    public default T get(){
        return find().orElseGet(() -> create());
    }
    public Optional<T> find();
    public T create();
}
