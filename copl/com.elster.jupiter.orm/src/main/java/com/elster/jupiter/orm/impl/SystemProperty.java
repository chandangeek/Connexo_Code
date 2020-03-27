package com.elster.jupiter.orm.impl;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SystemProperty extends HasId, HasName {

    @Override
    long getId();

    @Override
    String getName();

    String getValue();
}
