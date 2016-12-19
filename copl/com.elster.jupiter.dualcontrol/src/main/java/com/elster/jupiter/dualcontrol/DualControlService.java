package com.elster.jupiter.dualcontrol;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface DualControlService {

    String COMPONENT_NAME = "DUC";
    String DUAL_CONTROL_APPROVE_CATEGORY = "DualControlApprove";
    String DUAL_CONTROL_GRANT_CATEGORY = "DualControlGrant";

    Monitor createMonitor();

    Optional<Monitor> getMonitor(long id);
}
