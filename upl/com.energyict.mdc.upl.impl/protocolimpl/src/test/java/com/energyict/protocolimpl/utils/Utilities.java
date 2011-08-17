package com.energyict.protocolimpl.utils;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.ModemPool;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.CommunicationProfileShadow;
import com.energyict.mdw.shadow.CommunicationProtocolShadow;
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;
import com.energyict.mdw.shadow.GroupShadow;
import com.energyict.mdw.shadow.ModemPoolShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.mdw.shadow.RtuTypeShadow;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.protocolimpl.siemens7ED62.SCTMDumpData;

public class Utilities {

    /**
     * ReadMeterReadings, ReadMeterEvents, ReadDemandValues, SendRtuMessage
     */
    public static String commProfile_All = "all";
    /**
     * ReadDemandValues
     */
    public static String commProfile_ReadDemandValues = "readDemandValues";
    /**
     * SendRtuMessage
     */
    public static String commProfile_SendRtuMessage = "sendRtuMessage";

    public static String emptyGroup = "emptyGroup" + System.currentTimeMillis();
    public static String notEmptyGroup = "notEmptyGroup" + System.currentTimeMillis();
    public static String emptyUserFile = "emptyUserFile" + System.currentTimeMillis();
    public static String notEmptyUserFile = "notEmptyUserFile" + System.currentTimeMillis();
    public static String dummyModemPool = "dummyModemPool" + System.currentTimeMillis();

    /**
     * Create a new default {@link Environment}
     */
    public static void createEnvironment() {
        try {
            Properties properties = new Properties();
            properties.load(Utils.class.getResourceAsStream("/eiserver.properties"));
            Environment.setDefault(properties);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Create a communicationprotocol from a given JavaClassName
     *
     * @param javaClassName
     * @return the newly created communicationprotocol
     * @throws BusinessException
     * @throws SQLException
     */
    public static CommunicationProtocol findOrcreateCommunicationProtocol(String javaClassName) throws BusinessException, SQLException {
        List<CommunicationProtocol> comProtocols = mw().getCommunicationProtocolFactory().findByName(javaClassName);
        if(comProtocols.size() >= 1) {
            return comProtocols.get(0);
        }
        CommunicationProtocolShadow commProtShadow = new CommunicationProtocolShadow();
        commProtShadow.setJavaClassName(javaClassName);
        commProtShadow.setName(javaClassName);
        return mw().getCommunicationProtocolFactory().create(commProtShadow);
    }

    /**
     * Create an RtuType to use in future code as a basic to create new rtu's
     *
     * @param commProtocol - the protocol
     * @param name         - name
     * @param channelCount
     * @return the newly created RtuType
     * @throws SQLException
     * @throws BusinessException
     */
    public static RtuType createRtuType(CommunicationProtocol commProtocol, String name, int channelCount) throws SQLException, BusinessException {
        RtuTypeShadow rtuTypeShadow = new RtuTypeShadow();
        rtuTypeShadow.setChannelCount(channelCount);
        rtuTypeShadow.setName(name);
        rtuTypeShadow.setProtocolId(commProtocol.getId());
        RtuType rtuType = mw().getRtuTypeFactory().create(rtuTypeShadow);
        return rtuType;
    }

    /**
     * Create a basic Rtu with the serialNumber equal to "99999999, interval 3600s
     *
     * @param rtuType - the metertype of your wanted rtu
     * @return the newly created rtu
     * @throws SQLException
     * @throws BusinessException
     */
    public static Rtu createRtu(RtuType rtuType) throws SQLException, BusinessException {
        return createRtu(rtuType, "99999999");
    }

    /**
     * Create a basic Rtu with you given serialnumber and an interval of 3600s
     *
     * @param rtuType
     * @param serial  of your rtu
     * @return the newly created rtu
     * @throws SQLException
     * @throws BusinessException
     */
    public static Rtu createRtu(RtuType rtuType, String serial) throws SQLException, BusinessException {
        return createRtu(rtuType, serial, 3600);
    }

    /**
     * Create your custom Rtu with a given serialnumber and interval
     *
     * @param rtuType
     * @param serial   of your rtu
     * @param interval in seconds
     * @return the newly created rtu
     * @throws SQLException
     * @throws BusinessException
     */
    public static Rtu createRtu(RtuType rtuType, String serial, int interval) throws SQLException, BusinessException {
        final RtuShadow rtuShadow = rtuType.newRtuShadow();
        rtuShadow.setRtuTypeId(rtuType.getId());
        rtuShadow.setName(serial);
        rtuShadow.setExternalName(serial);
        rtuShadow.setIntervalInSeconds(interval);
        rtuShadow.setSerialNumber(serial);
        Rtu rtu = mw().getRtuFactory().create(rtuShadow);
        return rtu;
    }

    /**
     * Add a custom property to your given rtu
     *
     * @param rtu
     * @param key   - String name of the property
     * @param value - Logically the value of the property
     * @return the given rtu with the extra custom property
     * @throws SQLException
     * @throws BusinessException
     */
    public static Rtu addPropertyToRtu(Rtu rtu, String key, String value) throws SQLException, BusinessException {
        RtuShadow rtuShadow = rtu.getShadow();
        rtuShadow.getProperties().setProperty(key, value);
        rtu.delete();
        rtu = mw().getRtuFactory().create(rtuShadow);
        return rtu;
    }

    /**
     * Adds a channel to the given rtu
     *
     * @param rtu
     * @param intervalIndex - Use '5' for Days and '2' for Months
     * @param profileIndex
     * @return the given rtu with the extra channel
     * @throws BusinessException
     * @throws SQLException
     */
    public static Rtu addChannel(Rtu rtu, int intervalIndex, int profileIndex) throws BusinessException, SQLException {
        RtuShadow rtuShadow = rtu.getShadow();
        ChannelShadow channelShadow = new ChannelShadow();
        channelShadow.setName("Channel" + profileIndex);
        channelShadow.setInterval(new TimeDuration(1, intervalIndex));
        channelShadow.setLoadProfileIndex(profileIndex);
        rtuShadow.add(channelShadow);
        rtu.delete();
        rtu = mw().getRtuFactory().create(rtuShadow);
        return rtu;
    }

    /**
     * @return new dialer
     */
    public static Dialer getNewDialer() throws LinkException, IOException {
        Dialer dialer = null;
        dialer = DialerFactory.getDirectDialer().newDialer();
        dialer.init("COM1");
        dialer.connect("", 60000);
        dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                SerialCommunicationChannel.DATABITS_8,
                SerialCommunicationChannel.PARITY_NONE,
                SerialCommunicationChannel.STOPBITS_1);
        return dialer;
    }

    /**
     * Getter for the current {@link MeteringWarehouse}
     *
     * @return the current {@link MeteringWarehouse}
     */
    public static MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    /**
     * Get a {@link Channel} from a {@link Rtu}, using the channelindex
     *
     * @param rtu
     * @param index
     * @return The {@link Channel}
     */
    public static Channel getChannelWithProfileIndex(Rtu rtu, int index) {
        Iterator it = rtu.getChannels().iterator();
        while (it.hasNext()) {
            Channel chn = (Channel) it.next();
            if (chn.getLoadProfileIndex() == index) {
                return chn;
            }
        }
        return null;
    }

    /**
     * Create a new {@link CommunicationProfile} given a type name
     * The types can be:
     *
     * @param type
     * @return
     * @throws SQLException
     * @throws BusinessException
     */
    public static CommunicationProfile createCommunicationProfile(String type) throws SQLException, BusinessException {
        CommunicationProfileShadow cps = new CommunicationProfileShadow();
        if (type.equals(commProfile_All)) {
            cps.setName(commProfile_All);
            cps.setReadAllDemandValues(true);
            cps.setReadDemandValues(true);
            cps.setReadMeterEvents(true);
            cps.setReadMeterReadings(true);
            cps.setSendRtuMessage(true);
            cps.setStoreData(true);
        } else if (type.equals(commProfile_SendRtuMessage)) {
            cps.setName(commProfile_SendRtuMessage);
            cps.setSendRtuMessage(true);
            cps.setStoreData(true);
        } else if (type.equals(commProfile_ReadDemandValues)) {
            cps.setName(commProfile_ReadDemandValues);
            cps.setReadDemandValues(true);
            cps.setStoreData(true);
        }
        return mw().getCommunicationProfileFactory().create(cps);

    }

    /**
     * Create a new {@link CommunicationProfile} given a type name and ad it to a given {@link Rtu}
     * The types can be:
     *
     * @param rtu
     * @param type
     * @throws SQLException
     * @throws BusinessException
     */
    public static void createCommunicationScheduler(Rtu rtu, String type) throws SQLException, BusinessException {
        CommunicationSchedulerShadow css = new CommunicationSchedulerShadow();
        css.setCommunicationProfileId(createCommunicationProfile(type).getId());
        css.setRtuId(rtu.getId());
        ModemPool mp = createDummyModemPool();
        css.setModemPoolId(mp.getId());
        List schedulerShadows = new ArrayList(mp.getId());
        schedulerShadows.add(css);
        RtuShadow rtuShadow = rtu.getShadow();
        rtuShadow.setCommunicationSchedulerShadows(schedulerShadows);
        rtu.update(rtuShadow);
    }

    /**
     * @return
     * @throws SQLException
     * @throws BusinessException
     */
    public static Group createEmptyRtuGroup() throws SQLException, BusinessException {
        GroupShadow grs = new GroupShadow();
        grs.setName(emptyGroup);
//		grs.setObjectType(mw().getRtuFactory().getId());
        grs.getSearchFilter().setUseMaxResults(false);
        return mw().getGroupFactory().create(grs);
    }

    /**
     * Create a Group with some members in it
     *
     * @return
     * @throws BusinessException
     * @throws SQLException
     */
    public static Group createRtuTypeGroup() throws SQLException, BusinessException {
        GroupShadow grs = new GroupShadow();
        grs.setName(notEmptyGroup);
        grs.setObjectType(MeteringWarehouse.FACTORYID_RTU);
        grs.getSearchFilter().setObjectType(MeteringWarehouse.FACTORYID_RTU);
        grs.getSearchFilter().setUseMaxResults(false);
        return mw().getGroupFactory().create(grs);
    }

    /**
     * @return
     * @throws SQLException
     * @throws BusinessException
     */
    public static UserFile createEmptyUserFile() throws SQLException, BusinessException {
        UserFileShadow ufs = new UserFileShadow();
        ufs.setName(emptyUserFile);
        ufs.setExtension("bin");
        return mw().getUserFileFactory().create(ufs);
    }

    /**
     * Create a dummy {@link UserFile}
     *
     * @param userFile - the file to load into the userFile
     * @return the UserFile
     * @throws SQLException      if the creation didn't succeed because of a DataBase error
     * @throws BusinessException a business error occurred
     */
    public static UserFile createDummyNotEmptyUserFile(File userFile) throws SQLException, BusinessException {
        UserFileShadow ufs = new UserFileShadow();
        ufs.setName(notEmptyUserFile);
        ufs.setExtension("bin");
        ufs.setFile(userFile);
        return mw().getUserFileFactory().create(ufs);
    }

    /**
     * @return
     * @throws SQLException
     * @throws BusinessException
     */
    public static ModemPool createDummyModemPool() throws SQLException, BusinessException {
        List<ModemPool> result = mw().getModemPoolFactory().findByName(dummyModemPool);
        if (result.size() == 0) {
            ModemPoolShadow mps = new ModemPoolShadow();
            mps.setName(dummyModemPool);
            return mw().getModemPoolFactory().create(mps);
        } else {
            return result.get(0);
        }
    }

    /**
     * @param meter
     * @param date
     * @throws SQLException
     * @throws BusinessException
     */
    public static void changeLastReading(Rtu meter, Date date) throws SQLException, BusinessException {
        RtuShadow rs = meter.getShadow();
        rs.setLastReading(date);
        meter.update(rs);
    }

    /**
     * @param meter
     * @param date
     * @param channels
     * @throws SQLException
     * @throws BusinessException
     */
    public static void changeLastReading(Rtu meter, Date date, int[] channels) throws SQLException, BusinessException {
        RtuShadow rs = meter.getShadow();
        for (int i = 0; i < channels.length; i++) {
            if (rs.getChannelShadow(i) != null) {
                rs.getChannelShadow(i).setLastReading(date);
            }
        }
        meter.update(rs);
    }

    public static byte[] getBytesFromHexString(String hexString) {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += 3) {
            bb.write(Integer.parseInt(hexString.substring(i + 1, i + 3), 16));
        }
        return bb.toByteArray();
    }

    /**
     * Read the file into a String
     *
     * @param fileReader - the reader containing the file
     * @return a string
     * @throws IOException
     */
    public static String readWithStringBuffer(Reader fileReader) throws IOException {
        try {
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to readin the file." + "(" + e.getMessage() + ")");
        }
    }

    /**
     * Read the resourceFile
     *
     * @param resourceUrl the URL to the resourceFile
     * @return the byteArray content of the resourceFile
     * @throws IOException if something fishy happened during the reading of the resource
     */
    public static byte[] readResource(String resourceUrl) throws IOException {
        File file = new File(SCTMDumpData.class.getClassLoader().getResource(resourceUrl).getFile());
        FileInputStream fis = new FileInputStream(file);
        byte[] content = new byte[(int) file.length()];
        fis.read(content);
        fis.close();
        return content;
    }

    /**
     * Set the lastReading of all channels from the given <CODE>Rtu</CODE> to null.
     * @param rtu the <CODE>Rtu</CODE> whos channels need to be cleared
     * @throws BusinessException
     * @throws SQLException
     */
    public static void clearChannelsLastReading(Rtu rtu) throws BusinessException, SQLException {
        RtuShadow rShadow = rtu.getShadow();
        rShadow.setLastReading(new Date(1));
        rtu.update(rShadow);
        for (int i =    0; i < rtu.getChannels().size(); i++) {
            Channel chn = rtu.getChannel(i);
            chn.updateLastReading(null);
        }
    }
}
