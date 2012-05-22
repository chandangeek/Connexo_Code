/**
 *
 */
package com.energyict.genericprotocolimpl.iskragprs;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 *         <p/>
 *         Changes:
 *         GNA |23022009| Added connect/disconnect message; Added setVIF message to complete the install procedure
 */
@Deprecated
/** Jan 2012: If the IskraMx37x protocol is replaced by its smart version (com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372),
 the protocol for the Mbus device should also be changed to its smart alternative (com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.MbusDevice) **/
public class MbusDevice implements Messaging, GenericProtocol {

    private int mbusAddress = -1;        // this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
    private int physicalAddress = -1;    // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private int medium = 15;

    private String customerID;
    private String rtuType;
    private Unit mbusUnit;

    public Rtu mbus;
    private Logger logger;

    private ObisCode valveState = ObisCode.fromString("0.0.128.30.31.255");
    private ObisCode valveControl = ObisCode.fromString("0.0.128.30.30.255");

    private IskraMx37x iskra;

    /**
     *
     */
    public MbusDevice() {
    }

    public MbusDevice(int address, String customerID, Rtu rtu, Logger logger) throws InvalidPropertyException, MissingPropertyException {
        this.mbusAddress = address;
        this.customerID = customerID;
        this.mbus = rtu;
        this.logger = logger;
        if (mbus != null) {
            addProperties(mbus.getProperties());
        }
    }

    public MbusDevice(int mbusAddress, int phyAddress, String serial, int mbusMedium, Rtu rtu, Unit unit, Logger logger) throws InvalidPropertyException, MissingPropertyException {
        this.mbusAddress = mbusAddress;
        this.physicalAddress = phyAddress;
        this.customerID = serial;
        this.medium = mbusMedium;
        this.mbus = rtu;
        this.mbusUnit = unit;
        this.logger = logger;
        if (mbus != null) {
            addProperties(mbus.getProperties());
        }
    }


    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        CommunicationProfile commProfile = scheduler.getCommunicationProfile();
        // import profile
        if (commProfile.getReadDemandValues()) {
            MbusProfile mp = new MbusProfile(this);
            mp.getProfile(iskra.getMbusLoadProfile(getPhysicalAddress()));
        }

        // import Daily/Monthly registers
        if (commProfile.getReadMeterReadings()) {
            MbusDailyMonthly mdm = new MbusDailyMonthly(this);
            mdm.getDailyValues(iskra.getDailyLoadProfile());
            mdm.getMonthlyValues(iskra.getMonthlyLoadProfile());
        }

        // send RtuMessages
        if (commProfile.getSendRtuMessage()) {
            sendMeterMessages();
        }
    }

    public void addProperties(Properties properties) {
        try {
            setProperties(properties);
        } catch (InvalidPropertyException e) {
            e.printStackTrace();
        } catch (MissingPropertyException e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return getProtocolVersion();
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        rtuType = properties.getProperty("RtuType", "mbus");
    }

    public List getOptionalKeys() {
        return new ArrayList(0);
    }

    public List getRequiredKeys() {
        return new ArrayList(0);
    }

    @Override
    public void addProperties(TypedProperties properties) {
        rtuType = (String) properties.getProperty("RtuType", "mbus");
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");

        MessageSpec msgSpec = addBasicMsg("ReadOnDemand", RtuMessageConstant.READ_ON_DEMAND, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addVifMsg("Set vif to mbus device", RtuMessageConstant.MBUS_SET_VIF, false);
        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);

        return theCategories;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addVifMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public Rtu getMbus() {
        return mbus;
    }

    public void sendMeterMessages() throws IOException, BusinessException, SQLException {
        Iterator mi = mbus.getPendingMessages().iterator();

        while (mi.hasNext()) {
            RtuMessage msg = (RtuMessage) mi.next();
            String msgString = msg.getContents();
            String contents = msgString.substring(msgString.indexOf("<") + 1, msgString.indexOf(">"));
            if (contents.endsWith("/")) {
                contents = contents.substring(0, contents.length() - 1);
            }

            boolean ondemand = contents.equalsIgnoreCase(RtuMessageConstant.READ_ON_DEMAND);
            boolean doDisconnect = contents.toLowerCase().indexOf(RtuMessageConstant.DISCONNECT_LOAD.toLowerCase()) != -1;
            boolean doConnect = (contents.toLowerCase().indexOf(RtuMessageConstant.CONNECT_LOAD.toLowerCase()) != -1) && !doDisconnect;
            boolean mbusSetVIF = contents.equalsIgnoreCase(RtuMessageConstant.MBUS_SET_VIF);

            String description = "Getting ondemand registers for MBus device with serailnumber: " + getMbus().getSerialNumber();
            try {
                if (ondemand) {
                    getLogger().log(Level.INFO, description);
                    Iterator i = mbus.getRtuType().getRtuRegisterSpecs().iterator();
                    while (i.hasNext()) {

                        RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                        ObisCode oc = spec.getObisCode();
                        RtuRegister register = mbus.getRegister(oc);

                        if (register != null) {

                            if (oc.getF() == 255) {
                                RegisterValue rv = iskra.readRegister(oc);
                                rv.setRtuRegisterId(register.getId());

                                MeterReadingData meterReadingData = new MeterReadingData();
                                meterReadingData.add(rv);
                                mbus.store(meterReadingData);

                                /*register.add(rv.getQuantity().getAmount(), rv
                                                .getEventTime(), rv.getFromTime(), rv.getToTime(),
                                                rv.getReadTime());*/
                            }
                        } else {
                            String obis = oc.toString();
                            String msgError = "Register " + obis + " not defined on device";
                            getLogger().info(msgError);
                        }
                    }
                    msg.confirm();
                } else if (doDisconnect) {
                    Unsigned8 channel = new Unsigned8(getPhysicalAddress() + 1);
                    iskra.getCosemObjectFactory().getData(valveControl).setValueAttr(channel);

                    Unsigned8 state = new Unsigned8(0);
                    iskra.getCosemObjectFactory().getData(valveState).setValueAttr(state);

                    msg.confirm();
                } else if (doConnect) {

                    Unsigned8 channel = new Unsigned8(getPhysicalAddress() + 1);
                    iskra.getCosemObjectFactory().getData(valveControl).setValueAttr(channel);

                    Unsigned8 state = new Unsigned8(1);
                    iskra.getCosemObjectFactory().getData(valveState).setValueAttr(state);

                    msg.confirm();

                } else if (mbusSetVIF) {
                    String vif = iskra.getMessageValue(msgString, RtuMessageConstant.MBUS_SET_VIF);
                    if (vif.length() != 16) {
                        throw new IOException("VIF must be 8 characters long.");
                    }
                    iskra.getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0." + (getPhysicalAddress() + 1) + ".128.50.30.255"), 2, 1).write(OctetString.fromByteArray(ParseUtils.hexStringToByteArray(vif)).getBEREncodedByteArray());
                    msg.confirm();
                } else {
                    msg.setFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail(e, msg, description);
            }
        }
    }

    protected void fail(Exception e, RtuMessage msg, String description) throws BusinessException, SQLException {
        msg.setFailed();
        Rtu concentrator = getMbus().getGateway();
        if (concentrator != null) {
            List schedulers = concentrator.getCommunicationSchedulers();
            if (schedulers.size() > 0) {
                CommunicationScheduler scheduler = (CommunicationScheduler) schedulers.get(0);
                if (scheduler != null) {
                    AMRJournalManager amrJournalManager =
                            new AMRJournalManager(concentrator, scheduler);
                    amrJournalManager.journal(
                            new AmrJournalEntry(AmrJournalEntry.DETAIL, description + ": " + e.toString()));
                    amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
                    amrJournalManager.updateRetrials();
                }
            }
        }
        getLogger().severe(e.toString());
    }

    public boolean isIskraMbusObisCode(ObisCode oc) {
        if ((oc.getA() == 0) && (oc.getC() == 128) && (oc.getD() == 50)) {
//			if((oc.getB() >= 1) && (oc.getB() <= 4)){
            if (oc.getB() == (getPhysicalAddress() + 1)) {
                if (((oc.getE() >= 0) && (oc.getE() <= 3)) ||
                        ((oc.getE() >= 20) && (oc.getE() <= 25)) ||
                        ((oc.getE() >= 30) && (oc.getE() <= 33))) {
                    return true;
                }
            }
        }

        return false;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getRtuType() {
        return rtuType;
    }

    public int getMbusAddress() {
        return mbusAddress;
    }

    public void setMbusAddress(int mbusAddress) {
        this.mbusAddress = mbusAddress;
    }

    public int getPhysicalAddress() {
        return physicalAddress;
    }

    public void setPhysicalAddress(int physicalAddress) {
        this.physicalAddress = physicalAddress;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public Unit getMbusUnit() {
        return mbusUnit;
    }

    public void setMbusUnit(Unit mbusUnit) {
        this.mbusUnit = mbusUnit;
    }

    public void setMbus(Rtu mbus) {
        this.mbus = mbus;
    }

    public void setIskraDevice(IskraMx37x iskraMx37x) {
        this.iskra = iskraMx37x;
    }

    public IskraMx37x getIskraDevice() {
        return this.iskra;
    }

    public long getTimeDifference() {
        // TODO Auto-generated method stub
        return 0;
    }
}
