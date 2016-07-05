package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceEstimationImpl implements DeviceEstimation {
    
    public enum Fields {
        DEVICE("device"),
        ACTIVE("active"),
        ESTRULESETACTIVATIONS("estimationRuleSetActivations")
        ;
        
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }
    
    private Reference<Device> device = ValueReference.absent();
    private boolean active = false;
    private List<DeviceEstimationRuleSetActivation> estimationRuleSetActivations = new ArrayList<>();

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    
    private final DataModel dataModel;
    private final EstimationService estimationService;

    @Inject
    public DeviceEstimationImpl(DataModel dataModel, EstimationService estimationService) {
        this.dataModel = dataModel;
        this.estimationService = estimationService;
    }
    
    DeviceEstimation init(Device device, boolean active) {
        this.device.set(device);
        this.active = active;
        return this;
    }
    
    @Override
    public boolean isEstimationActive() {
        return active;
    }

    @Override
    public Device getDevice() {
        return this.device.get();
    }

    @Override
    public void activateEstimation() {
        if (!active) {
            active = true;
            saveAndTouchParent();
        }
    }
    
    @Override
    public void deactivateEstimation() {
        if (active) {
            active = false;
            saveAndTouchParent();
        }
    }
    
    @Override
    public List<DeviceEstimationRuleSetActivation> getEstimationRuleSetActivations() {
        List<EstimationRuleSet> ruleSetsOnDeviceConfig = device.get().getDeviceConfiguration().getEstimationRuleSets();
        
        List<DeviceEstimationRuleSetActivation> returnList = ruleSetsOnDeviceConfig.stream()
                .map(r -> Pair.of(r, findEstimationRuleSetActivation(r)))
                .map(p -> p.getLast().orElseGet(() -> dataModel.getInstance(DeviceEstimationRuleSetActivationImpl.class).init(p.getFirst(), true, this)))//not saved intentionally
                .collect(Collectors.toList());

        List<DeviceEstimationRuleSetActivation> removedFromDeviceConfiguration = estimationRuleSetActivations.stream()
                .filter(ruleSetActivation -> !ruleSetsOnDeviceConfig.contains(ruleSetActivation.getEstimationRuleSet()))
                .collect(Collectors.toList());
        boolean removed = estimationRuleSetActivations.removeAll(removedFromDeviceConfiguration);
        if (removed) {
            save();//don't touch parent because nothing has been changed, just the lists were synchronized
        }
        
        return returnList;
    }
    
    @Override
    public void activateEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        applyEstimationRuleSet(estimationRuleSet, true);
    }
    
    @Override
    public void deactivateEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        applyEstimationRuleSet(estimationRuleSet, false);
    }
    
    public EstimationService getEstimationService() {
        return estimationService;
    }
    
    private void applyEstimationRuleSet(EstimationRuleSet estimationRuleSet, boolean active) {
        Optional<DeviceEstimationRuleSetActivation> ruleSetActivation = findEstimationRuleSetActivation(estimationRuleSet);
        if (ruleSetActivation.isPresent()) {
            if (ruleSetActivation.get().isActive() != active) {
                ruleSetActivation.get().setActive(active);
                saveAndTouchParent();
            }
        } else {
            estimationRuleSetActivations.add(dataModel.getInstance(DeviceEstimationRuleSetActivationImpl.class).init(estimationRuleSet, active, this));
            saveAndTouchParent();
        }
    }
    
    private Optional<DeviceEstimationRuleSetActivation> findEstimationRuleSetActivation(EstimationRuleSet estimationRuleSet) {
        return estimationRuleSetActivations.stream().filter(er -> er.getEstimationRuleSet().getId() == estimationRuleSet.getId()).findAny();
    }
    
    private void save() {
        if (createTime != null) {
            dataModel.update(this);
        } else {
            dataModel.persist(this);
        }
    }
    
    private void saveAndTouchParent() {
        save();
        dataModel.touch(device.get());
    }
}
