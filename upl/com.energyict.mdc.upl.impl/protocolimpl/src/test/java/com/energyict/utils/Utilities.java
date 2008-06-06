package com.energyict.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
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
		RtuShadow rtuShadow = rtuType.newRtuShadow();
		rtuShadow.markClean();
		rtuShadow.setName(rtuType.getName());
		rtuShadow.setSerialNumber("99999999");
		Rtu rtu = mw().getRtuFactory().create(rtuShadow);
		return rtu;
	}
	
	public static Rtu createRtu(RtuType rtuType, String serial) throws SQLException, BusinessException{
		final RtuShadow rtuShadow = rtuType.newRtuShadow();
		rtuShadow.setName(rtuType.getName());
		rtuShadow.setSerialNumber(serial);
		Rtu rtu = mw().getRtuFactory().create(rtuShadow);
		return rtu;
	}
	
	
    public static MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }
}
