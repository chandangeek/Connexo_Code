package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface Subsystem {

    String getId();

    String getName();

    String getVersion();

    List<Component> getComponents();

}
