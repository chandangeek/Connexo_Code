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

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.mbus.core.MBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author kvds
 */
public class Sharky770 extends MBus {


    RegisterFactory registerFactory=null;

    /**
     * Creates a new instance of Sharky770
     */
    public Sharky770() {
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
    protected List doTheGetOptionalKeys() {
        List list = new ArrayList();
        return list;
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "NOT YET IMPLEMENTED";
    }

    /**
     * The protocol version date
     */
    public String getProtocolVersion() {
        return "$Date: 2015-06-26 10:11:31 +0200 (Fri, 26 Jun 2015) $";
    }

    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

}