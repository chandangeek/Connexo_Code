package com.elster.jupiter.properties;

import com.google.common.base.Optional;

public interface FindById<T extends ListValueEntry> {

    public abstract Optional<T> findById(String id);

}
