package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.nta.dsmr23.topology.MeterTopology;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * All NTA protocols (non smart, e.g. AM100, and smart, e.g. DSMR2.3/DSMR4.0) may implement this interface
 * The methods are called in the register factory, log book reader and load profile builder, these can be shared between the NTA protocols.
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 15:37
 * Author: khe
 */
public interface NtaProtocol extends DeviceProtocol {

    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber);

    public DlmsSession getDlmsSession();

    public Logger getLogger();

    public int getPhysicalAddressFromSerialNumber(String serialNumber);

    public String getSerialNumberFromCorrectObisCode(ObisCode obisCode);

    public TimeZone getTimeZone();

    public Date getTime();

    public MeterTopology getMeterTopology();

}
