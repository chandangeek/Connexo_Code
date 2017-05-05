/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.properties.HasIdAndName;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(
        name = "com.elster.jupiter.metering.fsmprovider",
        service = {StateTransitionPropertiesProvider.class},
        property = {"name=FSM_PROVIDER"}, immediate = true)
public class StateTransitionPropertiesProviderImpl implements StateTransitionPropertiesProvider {

    private volatile MeteringService meteringService;
    private final static String CONNECTION_STATES = "connectionStates";
    private final static String METROLOGY_CONFIG = "metrologyConfigurations";

    public StateTransitionPropertiesProviderImpl(){
    }

    @Inject
    public StateTransitionPropertiesProviderImpl(MeteringService meteringService){
        this.meteringService = meteringService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean areProcessPropertiesAvailableForUP(Map<String, Object> processProperties, long id) {
        boolean result= false;
        UsagePoint usagePoint = meteringService.findUsagePointById(id).orElse(null);
        if(usagePoint != null){
            Optional<UsagePointConnectionState> currentConnectionState = usagePoint.getCurrentConnectionState();
            Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = usagePoint
                    .getCurrentEffectiveMetrologyConfiguration();
            if(currentConnectionState.isPresent() && metrologyConfiguration.isPresent()){
                boolean connectionCheck = true;
                if(processProperties.get(CONNECTION_STATES) != null) {
                    connectionCheck = List.class.isInstance(processProperties.get(CONNECTION_STATES)) && ((List<Object>) processProperties
                            .get(CONNECTION_STATES))
                            .stream()
                            .filter(HasIdAndName.class::isInstance)
                            .anyMatch(v -> ((HasIdAndName) v).getId()
                                    .toString()
                                    .equals(currentConnectionState.get().getConnectionState().getId()));
                }
                boolean metrologyConfigurationCheck = true;
                if(processProperties.get(METROLOGY_CONFIG) != null) {
                    metrologyConfigurationCheck = List.class.isInstance(processProperties.get(METROLOGY_CONFIG)) && ((List<Object>) processProperties
                            .get(METROLOGY_CONFIG))
                            .stream()
                            .filter(HasIdAndName.class::isInstance)
                            .anyMatch(v -> ((HasIdAndName) v).getId()
                                    .toString()
                                    .equals(String.valueOf(metrologyConfiguration.get()
                                            .getMetrologyConfiguration()
                                            .getId())));
                }
                result = connectionCheck && metrologyConfigurationCheck;
            }
        }
        return result;
    }

    public String getDeviceMRID(long id){
        return meteringService.findEndDeviceById(id).map(IdentifiedObject::getMRID).orElse(null);
    }

    public String getUsagePointMRID(long id){
        return meteringService.findUsagePointById(id).map(IdentifiedObject::getMRID).orElse(null);
    }
}
