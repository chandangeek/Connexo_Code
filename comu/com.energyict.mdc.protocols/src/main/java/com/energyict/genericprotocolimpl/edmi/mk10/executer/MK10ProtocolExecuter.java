/**
 * MK10ProtocolExecuter.java
 *
 * Created on 16-jan-2009, 09:16:11 by jme
 *
 */
package com.energyict.genericprotocolimpl.edmi.mk10.executer;

import com.energyict.genericprotocolimpl.edmi.mk10.MK10Push;
import com.energyict.mdc.protocol.api.device.Device;
import com.energyict.protocolimpl.edmi.mk10.MK10;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jme
 */
public class MK10ProtocolExecuter {

    private Device meter = null;
    private MK10Push mk10Push = null;
    private MK10 mk10Protocol = new MK10();
    private Properties properties = new Properties();

    public MK10ProtocolExecuter(MK10Push mk10Push) {
        this.mk10Push = mk10Push;
    }

    private MK10Push getMk10Push() {
        return mk10Push;
    }

    private Logger getLogger() {
        return getMk10Push().getLogger();
    }

    protected void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    public Device getMeter() {
        return meter;
    }

    public void setMeter(Device meter) {
        this.meter = meter;
    }

    public MK10 getMk10Protocol() {
        return mk10Protocol;
    }

    public void addProperties(Properties properties) {
        this.properties.putAll(properties);
    }

    @SuppressWarnings("unchecked")
    public List<String> getOptionalKeys() {
        List<String> list = new ArrayList<>();
        list.addAll(getMk10Protocol().getOptionalKeys());
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRequiredKeys() {
        List<String> list = new ArrayList<>();
        list.addAll(getMk10Protocol().getRequiredKeys());
        return list;
    }

    public Properties getProperties() {
        return properties;
    }

}