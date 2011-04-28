package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocolimpl.messages.*;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.nta.elster.messagehandling.AM100MessageExecutor;
import com.energyict.genericprotocolimpl.nta.elster.profiles.DailyMonthlyProfile;
import com.energyict.genericprotocolimpl.nta.elster.profiles.ElectricityProfile;
import com.energyict.genericprotocolimpl.nta.elster.profiles.EventProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the subclass for the AM100 module.
 * The AM100 implements the NTA spec, but some things were not fully compliant with the original implementation.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28-mei-2010
 * Time: 13:56:15
 */
public class AM100 extends AbstractNTAProtocol {

    private static final ObisCode ACTIVE_FIRMWARE_OBISCODE = ObisCode.fromString("1.0.0.2.0.255");

    /** Only for testing */
    private static final boolean TESTING = false;

    /**
     * Property to indicate whether the cache (objectlist) <b>MUST</b> be read out
     */
    private boolean forcedToReadCache;

    /**
     * Fixed static string for the {@link #forcedToReadCache} property
     */
    private final static String PROP_FORCEDTOREADCACHE = "ForcedToReadCache";

    @Override
    protected String getFirmWareVersion() throws IOException {
        try {
            Data activeFirmware = getCosemObjectFactory().getData(ACTIVE_FIRMWARE_OBISCODE);
            return activeFirmware.getString();
//            return getCosemObjectFactory().getGenericRead(getMeterConfig().getVersionObject()).getString();
        } catch (IOException e) {
            log(Level.SEVERE, "Could not fetch the firmwareVersion: " + e.getMessage());
        }
        return "Unknown";
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced.<br>
     * <br>
     * <p/>
     * The AM100 module does not have the checkConfigParameter in his objectlist, thus to prevent reading the
     * objectlist each time we read the device, we will go for the following approach:<br>
     * 1/ check if the cache exists, if it does exist, go to step 2, if not go to step 3    <br>
     * 2/ is the custom property {@link #forcedToReadCache} enabled? If yes then go to step 3, else exit    <br>
     * 3/ readout the objectlist    <br>
     *
     * @throws java.io.IOException
     */
    @Override
    protected void checkCacheObjects() throws IOException {
        if ((super.dlmsCache.getObjectList() == null) || forcedToReadCache) {
            log(Level.INFO, forcedToReadCache ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            requestConfiguration();
            dlmsCache.saveObjectList(getMeterConfig().getInstantiatedObjectList());
        } else {
            log(Level.INFO, "Cache exist, will not be read!");
        }
    }
    
    /**
	 * Messages
	 * 
	 * @throws SQLException
	 * @throws BusinessException
     * @param rtuMessageList
	 */
    protected void sendMeterMessages(final List<RtuMessage> rtuMessageList) throws BusinessException, SQLException {

		AM100MessageExecutor messageExecutor = new AM100MessageExecutor(this);

		Iterator<RtuMessage> it = rtuMessageList.iterator();
		RtuMessage rm = null;
		while (it.hasNext()) {
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        MessageCategorySpec catDisconnect = getConnectControlCategory();
        MessageCategorySpec catInstallMbus = getInstallMbusCategory();

        categories.add(catDisconnect);
        categories.add(catInstallMbus);

        // We don't want those messages in the field
        if(TESTING){
            MessageCategorySpec catXMLConfig = getXmlConfigCategory();
            MessageCategorySpec catTime = getTimeCategory();
            MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
            MessageCategorySpec catTestMessage = getTestCategory();

            categories.add(catXMLConfig);
            categories.add(catTime);
            categories.add(catMakeEntries);
            categories.add(catTestMessage);
        }

        return categories;
    }

    /**
     * Create a message to install an MBus device. The value you need to enter is the equipmentIdentifier of the Mbus device
     * @return a category with 1 message related to mbus installation
     */
    public MessageCategorySpec getInstallMbusCategory() {
        MessageCategorySpec catTime = new MessageCategorySpec(
                RtuMessageCategoryConstants.MBUSSETUP);
        MessageSpec msgSpec = addMbusInstallMessage(RtuMessageKeyIdConstants.MBUSINSTALL,
                RtuMessageConstant.MBUS_INSTALL, false);
        catTime.addMessageSpec(msgSpec);
        return catTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doValidateProperties() {
        this.forcedToReadCache = !properties.getProperty(PROP_FORCEDTOREADCACHE, "0").equalsIgnoreCase("0");
        this.readMonthly = !properties.getProperty("ReadMonthlyValues", "0").equalsIgnoreCase("0");
        this.oldMbusDiscovery = Integer.parseInt(properties.getProperty("OldMbusDiscovery", "0"));
        this.fixMbusHexShortId =  Integer.parseInt(properties.getProperty("FixMbusHexShortId", "1")) != 0;
    }


    /**
     * Add a MBusDevice to the mbusDevices list
     *
     * @param index           - the index to put the mbus device
     * @param serial          - the serialnumber for the Mbus device
     * @param physicalAddress - the physicalAddress of the Mbus device
     * @param mbusRtu         - the Rtu from the database created for the Mbus device
     * @param logger          - the logger that will be used
     */
    @Override
    protected void addMbusDevice(int index, String serial, int physicalAddress, Rtu mbusRtu, Logger logger) {
        this.mbusDevices[index] = new MbusDevice(serial, physicalAddress, mbusRtu, logger);
    }

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters
     */
    @Override
    public List<String> doGetOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>(1);
        optionalKeys.add(PROP_FORCEDTOREADCACHE);
        return optionalKeys;
    }

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters
     */
    @Override
    public List<String> doGetRequiredKeys() {
        return null;
    }

    /**
     * Creates a new Instance of the the used MbusDevice type
     *
     * @param serial          the serialnumber of the mbusdevice
     * @param physicalAddress the physical address of the Mbus device
     * @param mbusRtu         the rtu in the database representing the mbus device
     * @param logger          the logger that will be used
     * @return a new Mbus class instance
     */
    @Override
    protected AbstractMbusDevice getMbusInstance(String serial, int physicalAddress, Rtu mbusRtu, Logger logger) {
        return new MbusDevice(serial, physicalAddress, mbusRtu, logger);  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
