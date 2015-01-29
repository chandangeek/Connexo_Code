package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;

import java.util.Arrays;
import java.util.List;

public enum SecurityPropertySetTpl implements Template<SecurityPropertySet, SecurityPropertySetBuilder> {
    NO_SECURITY("No security", 0, 0, Arrays.asList(
            DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2,
            DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2
    )),
    HIGH_LEVEL("High level authentication (MD5) and encryption", 3, 3, Arrays.asList(
            DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2,
            DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2
    ))
    ;
    private String name;
    private int authLevel;
    private int encLevel;
    private List<DeviceSecurityUserAction> userActions;

    SecurityPropertySetTpl(String name, int authLevel, int encLevel, List<DeviceSecurityUserAction> userActions) {
        this.name = name;
        this.authLevel = authLevel;
        this.encLevel = encLevel;
        this.userActions = userActions;
    }

    @Override
    public Class<SecurityPropertySetBuilder> getBuilderClass() {
        return SecurityPropertySetBuilder.class;
    }

    @Override
    public SecurityPropertySetBuilder get(SecurityPropertySetBuilder builder) {
        return builder.withAuthLevel(authLevel).withEncLevel(encLevel).withName(name).withUserActions(userActions);
    }

    public String getName() {
        return name;
    }
}
