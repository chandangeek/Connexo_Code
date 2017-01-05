package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Dialer;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.VirtualDeviceDialer;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Copyrights EnergyICT
 * Date: 21-apr-2010
 * Time: 11:15:21
 */
public class JIRA_COMMUNICATION_60 {

    private static final String FULL_DEBUG_1 = "jira_communication-60_01.log";
    private static final String FULL_DEBUG_2 = "jira_communication-60_02.log";
    private static final String FULL_DEBUG_3 = "jira_communication-60_03.log";
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");

    @Test
    public void profileTest_JIRA_COMMUNICATION60_1() {
        ProfileData pd = null;
        Date from = ProtocolTools.createCalendar(2010, 2, 27, 0, 0, 0, 0).getTime();
        Date to = ProtocolTools.createCalendar(2010, 3, 10, 0, 0, 0, 0).getTime();

        Properties properties = new Properties();
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty("Retries", "10");
        properties.setProperty("Timeout", "50");
        properties.setProperty("OPUS", "0");
        properties.setProperty("ForcedDelay", "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "--------");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "K94DS02874");

        try {
            PPM ppm = getPreparedPPMProtocol(getVirtualDeviceDialer(FULL_DEBUG_1, false), TIME_ZONE, properties);
            pd = ppm.getProfileData(from, to, true);
        } catch (Exception e) {
            log(e);
            fail("An unexpected error occured during the JUnit test");
        }

        assertNotNull(pd);
        assertEquals(4, pd.getNumberOfChannels());
        assertEquals(4, pd.getChannelInfos().size());

        assertEquals(Unit.get("kW"), pd.getChannel(0).getUnit());
        assertEquals(Unit.get("kW"), pd.getChannel(1).getUnit());
        assertEquals(Unit.get("kvar"), pd.getChannel(2).getUnit());
        assertEquals(Unit.get("kvar"), pd.getChannel(3).getUnit());

        assertEquals(502, pd.getNumberOfEvents());
        assertEquals(503, pd.getNumberOfIntervals());
        assertEquals(from, pd.getIntervalData(0).getEndTime());

    }

    @Test
    public void profileTest_JIRA_COMMUNICATION60_2() {
        ProfileData pd = null;
        Date from = ProtocolTools.createCalendar(2010, 3, 4, 0, 0, 0, 0).getTime();
        Date to = ProtocolTools.createCalendar(2010, 3, 10, 0, 0, 0, 0).getTime();

        Properties properties = new Properties();
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MAXTIMEDIFF.getName(), "300");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.MINTIMEDIFF.getName(), "1");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty("Retries", "10");
        properties.setProperty("Timeout", "50");
        properties.setProperty("OPUS", "0");
        properties.setProperty("ForcedDelay", "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "--------");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "K94DS02874");

        try {
            PPM ppm = getPreparedPPMProtocol(getVirtualDeviceDialer(FULL_DEBUG_2, false), TIME_ZONE, properties);
            pd = ppm.getProfileData(from, to, true);
        } catch (Exception e) {
            log(e);
        }

        assertNotNull(pd);
        assertEquals(4, pd.getNumberOfChannels());
        assertEquals(4, pd.getChannelInfos().size());

        assertEquals(Unit.get("kW"), pd.getChannel(0).getUnit());
        assertEquals(Unit.get("kW"), pd.getChannel(1).getUnit());
        assertEquals(Unit.get("kvar"), pd.getChannel(2).getUnit());
        assertEquals(Unit.get("kvar"), pd.getChannel(3).getUnit());

        assertEquals(from, pd.getIntervalData(0).getEndTime());
        assertEquals(263, pd.getNumberOfEvents());
        assertEquals(264, pd.getNumberOfIntervals());

    }

    @Test
    public void profileTest_JIRA_COMMUNICATION60_3() {
        ProfileData pd = null;
        Date from = ProtocolTools.createCalendar(2010, 2, 1, 0, 0, 0, 0).getTime();
        Date to = ProtocolTools.createCalendar(2010, 4, 22, 12, 0, 0, 0).getTime();

        Properties properties = new Properties();
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME.getName(), "0");
        properties.setProperty("Retries", "3");
        properties.setProperty("Timeout", "50");
        properties.setProperty("OPUS", "0");
        properties.setProperty("ForcedDelay", "0");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName(), "1800");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "--------");
        properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "--------K9302433");

        try {
            PPM ppm = getPreparedPPMProtocol(getVirtualDeviceDialer(FULL_DEBUG_3, false), TIME_ZONE, properties);
            ppm.getNumberOfChannels();
            pd = ppm.getProfileData(from, to, true);
        } catch (Exception e) {
            log(e);
            fail("An unexpected error occured during the JUnit test");
        }

        assertNotNull(pd);
        assertEquals(1, pd.getNumberOfChannels());
        assertEquals(1, pd.getChannelInfos().size());
        assertEquals(Unit.get("kW"), pd.getChannel(0).getUnit());
        assertEquals(from, pd.getIntervalData(0).getEndTime());
        assertEquals(1133, pd.getNumberOfEvents());
        assertEquals(3580, pd.getNumberOfIntervals());

    }

    /**
     * Create an instantation of the PPM issue 1 protocol, do the initialisation and connect to the device
     *
     * @param dialer
     * @param timeZone
     * @return
     * @throws IOException
     */
    private PPM getPreparedPPMProtocol(Dialer dialer, TimeZone timeZone, Properties properties) throws IOException {
        PPM ppm = new PPM(propertySpecService);
        ppm.setUPLProperties(properties);
        ppm.init(dialer.getInputStream(), dialer.getOutputStream(), timeZone, getLogger());
        ppm.enableHHUSignOn(dialer.getSerialCommunicationChannel(), false);
        ppm.connect();
        return ppm;
    }

    /**
     * Create a virtual device from a given 'Full debug file' generated by the CommServerJ
     *
     * @param fullDebugLogFileName
     * @param showCommunication
     * @return
     */
    private VirtualDeviceDialer getVirtualDeviceDialer(String fullDebugLogFileName, boolean showCommunication) {
        String debugFile = getClass().getResource(fullDebugLogFileName).getFile();
        VirtualDeviceDialer virtualDeviceDialer = new VirtualDeviceDialer(debugFile);
        virtualDeviceDialer.setShowCommunication(showCommunication);
        return virtualDeviceDialer;
    }

    /**
     * Get a logger for the current test class
     *
     * @return the logger
     */
    private Logger getLogger() {
        return Logger.getLogger(getClass().getCanonicalName());
    }

    private void log(Exception e) {
        getLogger().log(Level.SEVERE, "ERROR", e);
    }

}
