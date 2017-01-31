/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * S4.java
 *
 * Created on 17 oktober 2005, 8:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.ansi.c12.C12Layer2;
import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.PSEMServiceFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.itron.sentinel.logicalid.DataReadFactory;
import com.energyict.protocolimpl.itron.sentinel.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.itron.sentinel.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;
import com.energyict.protocolimpl.meteridentification.SentinelItron;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class Sentinel extends AbstractProtocol implements C12ProtocolLink {

    @Override
    public String getProtocolDescription() {
        return "Itron/Schlumberger Sentinel ANSI";
    }

    private C12Layer2 c12Layer2;
    private PSEMServiceFactory psemServiceFactory;
    private StandardTableFactory standardTableFactory;
    private ManufacturerTableFactory manufacturerTableFactory;
    private StandardProcedureFactory standardProcedureFactory;
    private ManufacturerProcedureFactory manufacturerProcedureFactory;
    SentinelItron sentinelItron=new SentinelItron();
    SentinelLoadProfile sentinelLoadProfile;

    private DataReadFactory dataReadFactory=null;

    String c12User;
    int c12UserId;
    int maxNrPackets;


    private ObisCodeInfoFactory obisCodeInfoFactory=null;

    @Inject
    public Sentinel(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading,new Date(),includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return sentinelLoadProfile.getProfileData(from,to,includeEvents);
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        return getDataReadFactory().getConstantsDataRead().getCustomerSerialNumber();

    }

    protected void validateSerialNumber() throws IOException {
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getStandardTableFactory().getManufacturerIdentificationTable().getManufacturerSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    public AbstractManufacturer getManufacturer() {
        return sentinelItron;
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {

            commChannel.setParams(9600,
                                  SerialCommunicationChannel.DATABITS_8,
                                  SerialCommunicationChannel.PARITY_NONE,
                                  SerialCommunicationChannel.STOPBITS_1);
            if (getDtrBehaviour() == 0) {
                commChannel.setDTR(false);
            }
            else if (getDtrBehaviour() == 1) {
                commChannel.setDTR(true);
            }
        }

        if ((getInfoTypeSecurityLevel()!=2) && ((getInfoTypePassword()==null) || (getInfoTypePassword().compareTo("")==0))) {
            setInfoTypePassword(new String(new byte[]{0}));
        }

      //identify with node 1 before you can address other nodes
        if (c12Layer2.getIdentity()!=1) {
        	int targetIdentity = c12Layer2.getIdentity();
        	c12Layer2.setIdentity(1);
        	getPSEMServiceFactory().getIdentificationResponse().getIdentificationFeature0();
        	getPSEMServiceFactory().terminate();
        	c12Layer2.setIdentity(targetIdentity);
        }
        getPSEMServiceFactory().logOn(c12UserId,replaceSpaces(c12User),getInfoTypePassword(),getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_ASCII, 128, maxNrPackets);
    }

    private String replaceSpaces(String c12User) {

        return c12User;

//        byte[] temp = new byte[10];
//        for (int i=0;i<temp.length;i++)
//            temp[i] = 0x20;
//        System.arraycopy(c12User.getBytes(), 0, temp, 0, c12User.getBytes().length);
//        String user = new String(temp);
//        return user.replace(' ', '\0');
    }


    protected void doDisConnect() throws IOException {
        getPSEMServiceFactory().logOff();
    }


    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"0"));
        c12User = properties.getProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getProperty("C12UserId","0").trim());
        maxNrPackets = Integer.parseInt(properties.getProperty("MaxNrPackets", "1"), 16);

    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList(
                    "C12User",
                    "C12UserId",
                    "MaxNrPackets");
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        setDataReadFactory(new DataReadFactory(manufacturerTableFactory));
        sentinelLoadProfile = new SentinelLoadProfile(this);
        return c12Layer2;
    }

    public void setTime() throws IOException {
        getStandardProcedureFactory().setDateTime();
//        throw new UnsupportedException("NOT IMPLEMENTED YET!");
    }

    public Date getTime() throws IOException {
        return getStandardTableFactory().getTime();
        //return getDataReadFactory().getCurrentStateDataRead().getCurrentTimeDate();
    }

    public int getNumberOfChannels() throws IOException {
        return getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return getStandardTableFactory().getManufacturerIdentificationTable().getManufacturer()+", "+
               getStandardTableFactory().getManufacturerIdentificationTable().getModel()+", "+
               "Firmware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getFwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getFwRevision()+", "+
               "Hardware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getHwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getHwRevision();
    }

    /*
     * Override this method if the subclass wants to set a specific register
     */
    public void setRegister(String name, String value) throws IOException {

    }

    /*
     * Override this method if the subclass wants to get a specific register
     */
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
    }



    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        int skip=0;
        StringBuilder strBuff = new StringBuilder();

/*
        strBuff.append(getStandardTableFactory().getConfigurationTable());
        strBuff.append(getStandardTableFactory().getActualLoadProfileTable());
        strBuff.append(getStandardTableFactory().getLoadProfileControlTable());
        strBuff.append(getStandardTableFactory().getLoadProfileStatusTable());
//        strBuff.append(getStandardTableFactory().getLoadProfileDataSetTable(0,0));
    	boolean skipIt=true;
    	if (skipIt) return strBuff.toString();
*/


        strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n");

        strBuff.append(getDataReadFactory().getConstantsDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCapabilitiesDataRead()).append("\n");

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getClockRelatedDataRead() + "\n");
        }

        strBuff.append(getDataReadFactory().getQuantityIdentificationDataRead()).append("\n");

        strBuff.append(getDataReadFactory().getCurrentStateDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentEnergyDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentDemandDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentCumulativeDemandDataRead()).append("\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getCurrentDemandTimeOfOccurenceDataRead() + "\n");
        }

        strBuff.append(getDataReadFactory().getLastBillingPeriodStateDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodEnergyDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodDemandDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodCumulativeDemandDataRead()).append("\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getLastBillingPeriodDemandTimeOfOccurenceDataRead() + "\n");
        }


        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates() > 0) {
            strBuff.append(getDataReadFactory().getLastSeasonStateDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonEnergyDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonDemandDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonCumulativeDemandDataRead()).append("\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
                strBuff.append(getDataReadFactory().getLastSeasonDemandTimeOfOccurenceDataRead() + "\n");
            }
        }

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasValidSelfReadData()) {
            strBuff.append(getDataReadFactory().getLastSelfReadStateDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadEnergyDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadDemandDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadCumulativeDemandDataRead()).append("\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
                strBuff.append(getDataReadFactory().getLastSelfReadDemandTimeOfOccurenceDataRead() + "\n");
            }
        }

        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels() > 0) {
            strBuff.append(getDataReadFactory().getLoadProfileQuantitiesDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLoadProfilePulseWeightsDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLoadProfilePreliminaryDataRead()).append("\n");

        }

        //strBuff.append(getDataReadFactory().getMeterMultiplierDataRead()+"\n");

        strBuff.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");

        while(true) {
            try {
                if (skip<=0) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=1) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=4) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getUnitOfMeasureEntryTable(true));}

                if (skip<=6) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConstantsTable(true));}
                if (skip<=9) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSourceDefinitionTable(true));}
                if (skip<=10) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCurrentRegisterDataTable(true));}
                if (skip<=13) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousSeasonDataTable(true));}
                if (skip<=14) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousDemandResetDataTable(true));}
                if (skip<=15) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSelfReadDataTable(true));}
                if (skip<=16) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPresentRegisterDataTable(true));}
                if (skip<=17) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=18) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=19) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCalendarTable());}
                if (skip<=20) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getClockTable());}
                if (skip<=21) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=22) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=23) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=24) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLogTable());}
                if (skip<=25) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=26) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogControlTable());}
if (skip<=27) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=28) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogControlTable());}
if (skip<=29) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableHeader());}
//                if (skip<=31) { skip++;strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getFeatureParameters());}
//                if (skip<=32) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getServiceTypeTable());}
//                if (skip<=33) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterFactors());}
//                if (skip<=34) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterStatus());}
//                if (skip<=35) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getS4Configuration());}
                if (skip<=36) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG
                strBuff.append("Table not supported! "+e.toString()+"\n");
            }
        }




        //System.out.println(strBuff.toString());
        return strBuff.toString();
    }


    /****************************************************************************************************************
     * Implementing C12ProtocolLink interface
     ****************************************************************************************************************/

    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }

    public int getProfileInterval() throws IOException {
        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()>0) {
            return getDataReadFactory().getLoadProfilePreliminaryDataRead().getLoadProfileIntervalLength() * 60;
        }
        else {
            return getInfoTypeProfileInterval();
        }
    }

    public TimeZone gettimeZone() {
        return super.getTimeZone();
    }

    public PSEMServiceFactory getPSEMServiceFactory() {
        return psemServiceFactory;
    }

    public ManufacturerTableFactory getManufacturerTableFactory() {
        return manufacturerTableFactory;
    }

    public StandardTableFactory getStandardTableFactory() {
        return standardTableFactory;
    }

    public StandardProcedureFactory getStandardProcedureFactory() {
        return standardProcedureFactory;
    }

    public ManufacturerProcedureFactory getManufacturerProcedureFactory() {
        return manufacturerProcedureFactory;
    }

    public int getMeterConfig() throws IOException {
        return -1; //getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory = new ObisCodeInfoFactory(this);
        }
        return obisCodeInfoFactory;
    }

    public DataReadFactory getDataReadFactory() {
        return dataReadFactory;
    }

    public void setDataReadFactory(DataReadFactory dataReadFactory) {
        this.dataReadFactory = dataReadFactory;
    }
}
