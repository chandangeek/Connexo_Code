package com.elster.jupiter.dualcontrol;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface DualControlService {

    String COMPONENT_NAME = "DUC";

    Monitor createMonitor();

    Optional<Monitor> getMonitor(long id);
}
