package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceQueryProvider;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Component(name = "com.elster.jupiter.metering.groups.impl.SimpleEndDeviceQueryProvider", service = {EndDeviceQueryProvider.class}, property = "name=" + MeteringGroupsService.COMPONENTNAME, immediate = true)
public class SimpleEndDeviceQueryProvider implements EndDeviceQueryProvider {

    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public static final String SIMPLE_ENDDEVICE_QUERYPRVIDER = SimpleEndDeviceQueryProvider.class.getName();

    @Override
    public String getName() {
        return SIMPLE_ENDDEVICE_QUERYPRVIDER;
    }

    @Override
    public List<EndDevice> findEndDevices(Condition conditions) {
        return findEndDevices(new Date(), conditions);
    }

    @Override
    public List<EndDevice> findEndDevices(Date date, Condition conditions) {
        return meteringService.getEndDeviceQuery().select(conditions);
    }
}
