/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Sharky770.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.hydrometer.sharky770;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.mbus.core.MBus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author kvds
 */
public class Sharky770 extends MBus {

    @Override
    public String getProtocolDescription() {
        return "Hydrometer Sharky 770 Mbus";
    }

    @Inject
    public Sharky770(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        // discovery is implemented in the GenericModbusDiscover protocol
        return null;
    }

    public Date getTime() throws IOException {
        return getRegisterFactory().getTime();
    }

    protected void doTheConnect() throws IOException {
        getMBusConnection().sendSND_NKE();
    }
    protected void doTheDisConnect() throws IOException {

    }
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

    }
    protected List<String> doTheGetOptionalKeys() {
        return Collections.emptyList();
    }

    public String getFirmwareVersion() throws IOException {
        return "NOT YET IMPLEMENTED";
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

}
