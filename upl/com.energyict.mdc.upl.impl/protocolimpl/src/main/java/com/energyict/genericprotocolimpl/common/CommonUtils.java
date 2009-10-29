package com.energyict.genericprotocolimpl.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.InvalidPropertyException;

/**
 * Commonly used methods for a {@link GenericProtocol}
 * 
 * @author gna
 *
 */
public final class CommonUtils {
	
	/**
	 * Find an Rtu by it's serialNumber in the database. The serialnumber is not unique so if multiples were found, exceptions will be thrown.
	 * If no rtu was found we will try to create one using the given RtuType. If the folderExtNameProperty is entered, then the new Rtu will be
	 * placed in that folder
	 * @param serialNumber - the SerialNumber of the Rtu
	 * @param rtuTypeProperty - the Prototype for the new Rtu if non was found
	 * @param folderExtNameProperty - the external name of the folder to place the new Rtu
	 * @return an Rtu
	 * @throws IOException - if multiple meters were found in the database
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	public static Rtu findOrCreateDeviceBySerialNumber(String serialNumber, String rtuTypeProperty, String folderExtNameProperty) throws IOException, SQLException, BusinessException{
		List result = mw().getRtuFactory().findBySerialNumber(serialNumber);
		if(result.size() == 1){		// we found the rtu so return it
			return (Rtu)result.get(0);	
		} else if(result.size() > 1){
			throw new IOException("Multple meters found in database with serialnumber " + serialNumber);
		} else {					// no results were found, try to create it
			RtuType rtuType = getRtuType(rtuTypeProperty);
			return createMeterWithSerialNumber(rtuType, serialNumber, folderExtNameProperty);
		}
	}
	
	/**
	 * Find an Rtu by it's deviceId in the database. The deviceId is not unique so if multiples were found, exceptions will be thrown.
	 * If no rtu was found we will try to create one using the given RtuType. If the folderExtNameProperty is entered, then the new Rtu will be
	 * placed in that folder
	 * @param deviceId - the DeviceId of the Rtu
	 * @param rtuTypeProperty - the Prototype for the new Rtu if non was found
	 * @param folderExtNameProperty - the external name of the folder to place the new Rtu
	 * @return an Rtu
	 * @throws IOException - if multiple meters were found in the database
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	public static Rtu findOrCreateDeviceByDeviceId(String deviceId, String rtuTypeProperty, String folderExtNameProperty) throws IOException, SQLException, BusinessException{
		List result = mw().getRtuFactory().findByDeviceId(deviceId);
		if(result.size() == 1){		// we found the rtu so return it
			return (Rtu)result.get(0);	
		} else if(result.size() > 1){
			throw new IOException("Multple meters found in database with deviceId " + deviceId);
		} else {					// no results were found, try to create it
			RtuType rtuType = getRtuType(rtuTypeProperty);
			return createMeterWithDeviceId(rtuType, deviceId, folderExtNameProperty);
		}
	}
	
	/**
	 * Create a new Rtu an place it in the given folder
	 * @param rtuType - the {@link RtuType} for the new Rtu
	 * @param serialNumber - the serialNumber for the new Rtu
	 * @param folderExtNameProperty - the folder to place the new Rtu
	 * @return a new Rtu
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	public static Rtu createMeterWithSerialNumber(RtuType rtuType, String serialNumber, String folderExtNameProperty) throws SQLException, BusinessException{
		RtuShadow shadow = rtuType.newRtuShadow();
    	shadow.setName(serialNumber);
        shadow.setSerialNumber(serialNumber);
        
        if(folderExtNameProperty != null){
        	Folder result = mw().getFolderFactory().findByExternalName(folderExtNameProperty);
    		if(result != null){
    			shadow.setFolderId(result.getId());
    		} // else the new rtu will be placed in the prototype folder
        }// else the new rtu will be placed in the prototype folder
        
		return mw().getRtuFactory().create(shadow);
	}
	
	/**
	 * Create a new Rtu an place it in the given folder
	 * @param rtuType - the {@link RtuType} for the new Rtu
	 * @param deviceId - the deviceId for the new Rtu
	 * @param folderExtNameProperty - the folder to place the new Rtu
	 * @return a new Rtu
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	public static Rtu createMeterWithDeviceId(RtuType rtuType, String deviceId, String folderExtNameProperty) throws SQLException, BusinessException{
		RtuShadow shadow = rtuType.newRtuShadow();
    	shadow.setName("Device - " + deviceId);
        shadow.setDeviceId(deviceId);
        
        if(folderExtNameProperty != null){
        	Folder result = mw().getFolderFactory().findByExternalName(folderExtNameProperty);
    		if(result != null){
    			shadow.setFolderId(result.getId());
    		} // else the new rtu will be placed in the prototype folder
        }// else the new rtu will be placed in the prototype folder
        
		return mw().getRtuFactory().create(shadow);
	}
	
	/**
	 * Find the RtuType for the given RtuTypeName
	 * @param rtuTypeProperty - the name of the RtuType
	 * @return the found rtuType
	 * @throws InvalidPropertyException when the name of the rtuType is null, when no RtuType was found for the given name and when
	 *  the found RtuType doesn't have a prototype Rtu
	 */
	public static RtuType getRtuType(String rtuTypeProperty) throws InvalidPropertyException{
		if(rtuTypeProperty != null){
			RtuType rtuType = mw().getRtuTypeFactory().find(rtuTypeProperty);
			if(rtuType == null){
				throw new InvalidPropertyException("No rtutype defined with name '" + rtuTypeProperty + "'.");
			} else if(rtuType.getPrototypeRtu() == null){
				throw new InvalidPropertyException("Rtutype '" + rtuTypeProperty + "' has no prototype rtu.");
			} else {
				return rtuType;
			}
		} else {
			throw new InvalidPropertyException("No automatic meter creation: no property RtuType defined.");
		}
	}

	/** Short notation for the current MeteringWarehous */
	public static MeteringWarehouse mw(){
		MeteringWarehouse result = MeteringWarehouse.getCurrent();
		return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
	}
	
	/**
	 * Check whether the {@link RtuRegisterGroup} from the given RtuRegister is in the given RtuRegisterGroup-List
	 * @param groups - a List of {@link RtuRegisterGroup}s
	 * @param rr - the {@link RtuRegister} to check
	 * @return true if the list contains the RtuRegisterGroup from the given RtuRegister, false otherwise
	 */
	public static boolean isInRegisterGroup(List<RtuRegisterGroup> groups, RtuRegister rr) {
		if (rr.getGroup() == null) {
			if (groups.size() == 0) {
				return true;
			}
			return false;
		}
		Iterator<RtuRegisterGroup> it = groups.iterator();
		while (it.hasNext()) {
			if (rr.getGroup().equals(it.next())) {
				return true;
			}
		}
		return false;
	}
}
