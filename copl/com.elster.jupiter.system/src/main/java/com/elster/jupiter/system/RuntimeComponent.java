package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface RuntimeComponent {

    long getId();

    String getName();

    ComponentStatus getStatus();

    Component getComponent();

    Subsystem getSubsystem();
}
