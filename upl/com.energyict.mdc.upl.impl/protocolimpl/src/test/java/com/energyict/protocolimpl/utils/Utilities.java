package com.energyict.protocolimpl.utils;

import java.io.IOException;
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

public class Utilities {
	
	/**
	 * ReadMeterReadings, ReadMeterEvents, ReadDemandValues, SendRtuMessage
	 */
	public static String COMMPROFILE_ALL = "all";
	/**
	 * ReadDemandValues
	 */
	public static String COMMPROFILE_READDEMANDVALUES = "readDemandValues";
	/**
	 * SendRtuMessage
	 */
	public static String COMMPROFILE_SENDRTUMESSAGE = "sendRtuMessage";
	
	public static String EMPTY_GROUP = "emptyGroup";
	public static String EMPTY_USERFILE = "emptyUserFile";
	public static String DUMMY_MODEMPOOL = "dummyModemPool";
	
	public static void createEnvironment() {
    	try {
    		Properties properties = new Properties();
    		properties.load(Utils.class.getResourceAsStream( "/eiserver.properties" ));
			Environment.setDefault(properties);
		} catch (IOException e) {
            throw new ApplicationException(e);
		}
    }
	
	/**
	 * Create a communicationprotocol from a given JavaClassName
	 * @param javaClassName
	 * @return the newly created communicationprotocol
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static CommunicationProtocol createCommunicationProtocol(String javaClassName) throws BusinessException, SQLException{
		CommunicationProtocolShadow commProtShadow = new CommunicationProtocolShadow();
		commProtShadow.setJavaClassName(javaClassName);
		commProtShadow.setName(javaClassName);
		return mw().getCommunicationProtocolFactory().create(commProtShadow);
	}
	
	/**
	 * Create an RtuType to use in future code as a basic to create new rtu's
	 * @param commProtocol - the protocol
	 * @param name - name
	 * @param channelCount
	 * @return the newly created RtuType
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static RtuType createRtuType(CommunicationProtocol commProtocol, String name, int channelCount) throws SQLException, BusinessException{
		RtuTypeShadow rtuTypeShadow = new RtuTypeShadow();
		rtuTypeShadow.setChannelCount(channelCount);
		rtuTypeShadow.setName(name);
		rtuTypeShadow.setProtocolId(commProtocol.getId());
		RtuType rtuType = mw().getRtuTypeFactory().create(rtuTypeShadow);
		return rtuType;
	}
	
	/**
	 * Create a basic Rtu with the serialNumber equal to "99999999, interval 3600s
	 * @param rtuType - the metertype of your wanted rtu
	 * @return the newly created rtu
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Rtu createRtu(RtuType rtuType) throws SQLException, BusinessException{
		return createRtu(rtuType, "99999999");
	}
	
	/**
	 * Create a basic Rtu with you given serialnumber and an interval of 3600s
	 * @param rtuType
	 * @param serial of your rtu
	 * @return the newly created rtu
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Rtu createRtu(RtuType rtuType, String serial) throws SQLException, BusinessException{
		return createRtu(rtuType, serial, 3600);
	}
	
	/**
	 * Create your custom Rtu with a given serialnumber and interval
	 * @param rtuType
	 * @param serial of your rtu
	 * @param interval in seconds
	 * @return the newly created rtu
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Rtu createRtu(RtuType rtuType, String serial, int interval) throws SQLException, BusinessException{
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
	 * @param rtu
	 * @param key - String name of the property
	 * @param value - Logically the value of the property
	 * @return the given rtu with the extra custom property
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Rtu addPropertyToRtu(Rtu rtu, String key, String value) throws SQLException, BusinessException{
		RtuShadow rtuShadow = rtu.getShadow();
		rtuShadow.getProperties().setProperty(key, value);
		rtu.delete();
		rtu = mw().getRtuFactory().create(rtuShadow);
		return rtu;
	}
	
	/**
	 * Adds a channel to the given rtu
	 * @param rtu
	 * @param intervalIndex	- Use '5' for Days and '2' for Months
	 * @param profileIndex
	 * @return the given rtu with the extra channel
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static Rtu addChannel(Rtu rtu, int intervalIndex, int profileIndex) throws BusinessException, SQLException{
		RtuShadow rtuShadow = rtu.getShadow();
		ChannelShadow channelShadow = new ChannelShadow();
		channelShadow.setName("Channel"+profileIndex);
		channelShadow.setInterval(new TimeDuration(1, intervalIndex));
		channelShadow.setLoadProfileIndex(profileIndex);
		rtuShadow.add(channelShadow);
		rtu.delete();
		rtu = mw().getRtuFactory().create(rtuShadow);
		return rtu;
	}
	
	/**
	 * @param COM1 - 9600 - NO Parity - 1 - 60000(timeOut in ms)
	 * @return new dialer
	 * 
	 */
	public static Dialer getNewDialer() throws LinkException, IOException{
		Dialer dialer=null;
        dialer =DialerFactory.getDirectDialer().newDialer();
        dialer.init("COM1");
        dialer.connect("",60000); 
        dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                SerialCommunicationChannel.DATABITS_8,
                SerialCommunicationChannel.PARITY_NONE,
                SerialCommunicationChannel.STOPBITS_1);
		return dialer;
	}
	
    public static MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }
    
	public static Channel getChannelWithProfileIndex(Rtu rtu, int index){
		Iterator it = rtu.getChannels().iterator();
		while(it.hasNext()){
			Channel chn = (Channel)it.next();
			if(chn.getLoadProfileIndex() == index)
				return chn;
		}
		return null;
	}

	public static CommunicationProfile createCommunicationProfile(String type) throws SQLException, BusinessException{
		CommunicationProfileShadow cps = new CommunicationProfileShadow();
		if(type.equals(COMMPROFILE_ALL)){
			cps.setName(COMMPROFILE_ALL);
			cps.setReadAllDemandValues(true);
			cps.setReadDemandValues(true);
			cps.setReadMeterEvents(true);
			cps.setReadMeterReadings(true);
			cps.setSendRtuMessage(true);
			cps.setStoreData(true);
		} else if(type.equals(COMMPROFILE_SENDRTUMESSAGE)){
			cps.setName(COMMPROFILE_SENDRTUMESSAGE);
			cps.setSendRtuMessage(true);
			cps.setStoreData(true);
		} else if(type.equals(COMMPROFILE_READDEMANDVALUES)){
			cps.setName(COMMPROFILE_READDEMANDVALUES);
			cps.setReadDemandValues(true);
			cps.setStoreData(true);
		}
		return mw().getCommunicationProfileFactory().create(cps);
		
	}
	
	public static void createCommunicationScheduler(Rtu rtu, String type) throws SQLException, BusinessException {
		CommunicationSchedulerShadow css = new CommunicationSchedulerShadow();
		css.setCommunicationProfile(createCommunicationProfile(type));
		css.setRtuId(rtu.getId());
		ModemPool mp = createDummyModemPool();
		css.setModemPoolId(mp.getId());
		List schedulerShadows = new ArrayList(mp.getId());
		schedulerShadows.add(css);
		RtuShadow rtuShadow = rtu.getShadow();
		rtuShadow.setCommunicationSchedulerShadows(schedulerShadows);
		rtu.update(rtuShadow);
	}
	
	public static Group createEmptyRtuGroup() throws SQLException, BusinessException{
		GroupShadow grs = new GroupShadow();
		grs.setName(EMPTY_GROUP);
		grs.setObjectType(mw().getRtuFactory().getId());
		return mw().getGroupFactory().create(grs);
	}

	public static UserFile createEmptyUserFile() throws SQLException, BusinessException {
		UserFileShadow ufs = new UserFileShadow();
		ufs.setName(EMPTY_USERFILE);
		ufs.setExtension("bin");
		return mw().getUserFileFactory().create(ufs);
	}
	
	public static ModemPool createDummyModemPool() throws SQLException, BusinessException{
		List<ModemPool> result = mw().getModemPoolFactory().findByName(DUMMY_MODEMPOOL);
		if(result.size() == 0){
			ModemPoolShadow mps = new ModemPoolShadow();
			mps.setName(DUMMY_MODEMPOOL);
			return mw().getModemPoolFactory().create(mps);
		} else {
			return result.get(0);
		}
	}
	
	public static void changeLastReading(Rtu meter, Date date) throws SQLException, BusinessException{
		RtuShadow rs = meter.getShadow();
		rs.setLastReading(date);
		meter.update(rs);
	}
	
	public static void changeLastReading(Rtu meter, Date date, int[] channels) throws SQLException, BusinessException{
		RtuShadow rs = meter.getShadow();
		for(int i = 0; i < channels.length; i++){
			if(rs.getChannelShadow(i) != null){
				rs.getChannelShadow(i).setLastReading(date);
			}
		}
		meter.update(rs);
	}
}
