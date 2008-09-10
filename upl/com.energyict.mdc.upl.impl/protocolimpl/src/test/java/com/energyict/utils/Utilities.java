package com.energyict.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.ChannelShadow;
import com.energyict.mdw.shadow.CommunicationProtocolShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.mdw.shadow.RtuTypeShadow;

public class Utilities {
	
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
		rtuShadow.setName(rtuType.getName());
		rtuShadow.setExternalName(rtuType.getName());
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
}
