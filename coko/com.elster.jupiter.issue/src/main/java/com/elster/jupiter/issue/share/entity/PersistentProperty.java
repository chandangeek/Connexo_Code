package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PersistentProperty {

    String getName();

    Object getValue();

    void setValue(Object value);

}
