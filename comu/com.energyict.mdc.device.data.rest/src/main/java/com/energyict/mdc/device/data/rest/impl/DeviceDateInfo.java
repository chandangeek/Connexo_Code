package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;

import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeviceDateInfo {
    public enum DateAttribute {
        SHIPMENT("date.shipment", Collections.<DefaultState>emptyList()) {
            @Override
            public Optional<Instant> getOptionalValue(CIMLifecycleDates dates) {
                return dates.getReceivedDate();
            }
        },
        INSTALLATION("date.installation", Arrays.asList(DefaultState.ACTIVE, DefaultState.INACTIVE, DefaultState.DECOMMISSIONED)) {
            @Override
            public Optional<Instant> getOptionalValue(CIMLifecycleDates dates) {
                return dates.getInstalledDate();
            }
        },
        DEACTIVATION("date.deactivation", Arrays.asList(DefaultState.INACTIVE)) {
            @Override
            public Optional<Instant> getOptionalValue(CIMLifecycleDates dates) {
                return dates.getRemovedDate();
            }
        },
        DECOMMISSIONING("date.decommissioning", Arrays.asList(DefaultState.DECOMMISSIONED)) {
            @Override
            public Optional<Instant> getOptionalValue(CIMLifecycleDates dates) {
                return dates.getRetiredDate();
            }
        },
        ;

        private String key;
        private List<DefaultState> availableInStates;

        DateAttribute(String key, List<DefaultState> availableInStates) {
            this.key = key;
            this.availableInStates = availableInStates;
        }

        protected abstract Optional<Instant> getOptionalValue(CIMLifecycleDates dates);

        public Instant getValue(CIMLifecycleDates dates){
            return dates != null ? getOptionalValue(dates).orElse(null) : null;
        }

        public String getDateName(){
            return this.key;
        }

        public boolean isMatchedCurrentState(State state){
            if (!this.availableInStates.isEmpty()){
                Optional<DefaultState> correspondingDefaultState = DefaultState.from(state);
                return correspondingDefaultState.isPresent()
                        && this.availableInStates.contains(correspondingDefaultState.get());
            }
            return true;
        }
    }

    public String name;
    public Instant timestamp;
    public boolean matchCurrentState;
}
