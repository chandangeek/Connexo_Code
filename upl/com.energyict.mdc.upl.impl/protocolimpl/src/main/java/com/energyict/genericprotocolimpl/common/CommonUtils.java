package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.BusinessObject;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.RtuFactoryImpl;
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.metadata.Criterium;
import com.energyict.metadata.TypeDescriptor;
import com.energyict.protocol.InvalidPropertyException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

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
	 * @throws IOException if multiple meters were found in the database
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
	 * @throws IOException if multiple meters were found in the database
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
     *
     * After creation we iterate over all the CommunicationSchedulers
     * of the device and set the next reading date to now, when the
     * auto reschedule is enabled and the next reading date is NULL.
     *
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

        Rtu rtu = mw().getRtuFactory().create(shadow);
        setNextCommunications(rtu);
        return rtu;
	}

	/**
	 * Create a new Rtu an place it in the given folder.
     *
     * After creation we iterate over all the CommunicationSchedulers
     * of the device and set the next reading date to now, when the
     * auto reschedule is enabled and the next reading date is NULL.
     *
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

        Rtu rtu = mw().getRtuFactory().create(shadow);
        setNextCommunications(rtu);
        return rtu;
	}

	/**
     * Iterate over all the CommunicationSchedulers of the slave device and
     * set the next reading date to now, when the auto reschedule is enabled
     * and the next reading date is NULL.
     *
     * @param slaveDevice
     * @throws SQLException
     * @throws BusinessException
     */
    private static void setNextCommunications(Rtu slaveDevice) throws SQLException, BusinessException {
        List<CommunicationScheduler> schedulers = slaveDevice.getCommunicationSchedulers();
        for (CommunicationScheduler scheduler : schedulers) {
            if (scheduler.getActive() && (scheduler.getNextCommunication() == null)) {
                CommunicationSchedulerShadow shadow = scheduler.getShadow();
                shadow.setNextCommunication(new Date());
                scheduler.update(shadow);
            }
        }
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
     * Find a Rtu for the given phoneNumber
     *
     * @param phoneNumber the device phoneNumber as String
     * @return one single Rtu
     * @throws IOException when no device or more then one device was found with the given phoneNumber
     */
    public static Rtu findDeviceByPhoneNumber(String phoneNumber) throws IOException {
        RtuFactoryImpl factory = (RtuFactoryImpl) mw().getRtuFactory();
        TypeDescriptor type = factory.getTypeDescriptor();
        com.energyict.metadata.SearchFilter filter = new com.energyict.metadata.SearchFilter(type);
        filter.addAnd(Criterium.eq(type.getAttributeDescriptor("phoneNumber"), phoneNumber));
        List<BusinessObject> result = factory.findBySearchFilter(filter);
        if (result.size() == 1) {
            return (Rtu) result.get(0);
        } else if (result.size() > 1) {
            throw new IOException("Multiple meters found in database with phoneNumber " + phoneNumber);
        } else {
            throw new IOException("No meter found in database with phoneNumber " + phoneNumber);
        }
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
