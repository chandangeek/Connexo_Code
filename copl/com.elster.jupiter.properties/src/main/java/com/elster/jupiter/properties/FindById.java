package com.elster.jupiter.properties;

import java.util.Optional;

public interface FindById<T extends ListValueEntry> {

    public abstract Optional<T> findById(String id);

}
