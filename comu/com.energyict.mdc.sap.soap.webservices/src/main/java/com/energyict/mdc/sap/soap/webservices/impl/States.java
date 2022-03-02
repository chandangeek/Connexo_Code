package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum States {
    SHIPMENT_DATE {
        @Override
        public List<DefaultState> attributeIsEditableForStates() {
            return Collections.singletonList(com.elster.jupiter.metering.DefaultState.IN_STOCK);
        }
    },
    INSTALLATION_DATE {
        @Override
        public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
            return Arrays.asList(
                    com.elster.jupiter.metering.DefaultState.COMMISSIONING,
                    com.elster.jupiter.metering.DefaultState.ACTIVE,
                    com.elster.jupiter.metering.DefaultState.INACTIVE
            );
        }
    },
    DEACTIVATION_DATE {
        @Override
        public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
            return Arrays.asList(com.elster.jupiter.metering.DefaultState.ACTIVE,
                    com.elster.jupiter.metering.DefaultState.COMMISSIONING,
                    com.elster.jupiter.metering.DefaultState.IN_STOCK
            );

        }
    },
    DECOMMISSIONING_DATE {
        @Override
        public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
            return Collections.singletonList(com.elster.jupiter.metering.DefaultState.DECOMMISSIONED);
        }

    };

    public List<com.elster.jupiter.metering.DefaultState> attributeIsEditableForStates() {
        return Arrays.asList(
                com.elster.jupiter.metering.DefaultState.IN_STOCK,
                com.elster.jupiter.metering.DefaultState.COMMISSIONING,
                com.elster.jupiter.metering.DefaultState.ACTIVE,
                com.elster.jupiter.metering.DefaultState.INACTIVE
        );
    }

    public boolean isEditableForState(State state) {
        if (this.attributeIsEditableForStates() != null) {
            Optional<DefaultState> correspondingDefaultState = com.elster.jupiter.metering.DefaultState.from(state);
            if (correspondingDefaultState.isPresent()) {
                return this.attributeIsEditableForStates().contains(correspondingDefaultState.get());
            }
            return true;
        }
        return false;
    }
}