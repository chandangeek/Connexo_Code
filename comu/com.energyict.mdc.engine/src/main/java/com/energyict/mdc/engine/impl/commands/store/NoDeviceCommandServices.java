package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;

import java.time.Clock;

/**
* Provides an implementation for the {@link DeviceCommand.ServiceProvider} interface
* for classes that actually do not need any services.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2015-01-13 (16:23)
*/
public class NoDeviceCommandServices implements DeviceCommand.ServiceProvider {
    @Override
    public IssueService issueService() {
        return null;
    }

    @Override
    public Clock clock() {
        return null;
    }

    @Override
    public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
        return null;
    }

    @Override
    public EngineService engineService() {
        return null;
    }

    @Override
    public EventService eventService() {
        return null;
    }

    @Override
    public NlsService nlsService() {
        return null;
    }
}