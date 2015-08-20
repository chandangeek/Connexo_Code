package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class SecurityPropertySetBuilder extends NamedBuilder<SecurityPropertySet, SecurityPropertySetBuilder> {

    private DeviceConfiguration deviceConfiguration;
    private int authLevel;
    private int encLevel;
    private List<DeviceSecurityUserAction> userActions;

    @Inject
    public SecurityPropertySetBuilder() {
        super(SecurityPropertySetBuilder.class);
    }

    public SecurityPropertySetBuilder withDeviceConfiguration(DeviceConfiguration deviceConfiguration){
        this.deviceConfiguration = deviceConfiguration;
        return this;
    }

    public SecurityPropertySetBuilder withAuthLevel(int authLevel){
        this.authLevel = authLevel;
        return this;
    }

    public SecurityPropertySetBuilder withEncLevel(int encLevel){
        this.encLevel = encLevel;
        return this;
    }

    public SecurityPropertySetBuilder withUserActions(List<DeviceSecurityUserAction> userActions){
        this.userActions = userActions;
        return this;
    }

    private void check(){
        if (this.deviceConfiguration == null) {
            throw new UnableToCreate("You must set the device configuration");
        }
    }

    @Override
    public Optional<SecurityPropertySet> find() {
        check();
        return deviceConfiguration.getSecurityPropertySets().stream().filter(sps -> sps.getName().equals(getName())).findFirst();
    }

    @Override
    public SecurityPropertySet create() {
        SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet(getName()).authenticationLevel(authLevel).encryptionLevel(encLevel).build();
        if (userActions != null) {
            for (DeviceSecurityUserAction userAction : userActions) {
                securityPropertySet.addUserAction(userAction);
            }
        }
        securityPropertySet.update();
        return securityPropertySet;
    }
}
