package com.energyict.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
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
	
	public static CommunicationProtocol createCommunicationProtocol(String javaClassName) throws BusinessException, SQLException{
		CommunicationProtocolShadow commProtShadow = new CommunicationProtocolShadow();
		commProtShadow.setJavaClassName(javaClassName);
		commProtShadow.setName(javaClassName);
		return mw().getCommunicationProtocolFactory().create(commProtShadow);
	}
	
	public static RtuType createRtuType(CommunicationProtocol commProtocol, String name, int channelCount) throws SQLException, BusinessException{
		RtuTypeShadow rtuTypeShadow = new RtuTypeShadow();
		rtuTypeShadow.setChannelCount(channelCount);
		rtuTypeShadow.setName(name);
//		rtuTypeShadow.setProtocolShadow(commProtocol.getShadow());
		rtuTypeShadow.setProtocolId(commProtocol.getId());
		RtuType rtuType = mw().getRtuTypeFactory().create(rtuTypeShadow);
		return rtuType;
	}
	
	public static Rtu createRtu(RtuType rtuType) throws SQLException, BusinessException{
		return createRtu(rtuType, "99999999");
	}
	
	public static Rtu createRtu(RtuType rtuType, String serial) throws SQLException, BusinessException{
		return createRtu(rtuType, serial, 3600);
	}
	
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
	
	public static Rtu addPropertyToRtu(Rtu rtu, String key, String value) throws SQLException, BusinessException{
		RtuShadow rtuShadow = rtu.getShadow();
		rtuShadow.getProperties().setProperty(key, value);
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
}
