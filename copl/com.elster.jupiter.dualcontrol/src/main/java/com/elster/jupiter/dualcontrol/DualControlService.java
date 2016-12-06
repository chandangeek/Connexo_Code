package com.elster.jupiter.dualcontrol;

import java.util.Optional;

public interface DualControlService {

    String COMPONENT_NAME = "DUC";

    Monitor createMonitor();

    Optional<Monitor> getMonitor(long id);
}
