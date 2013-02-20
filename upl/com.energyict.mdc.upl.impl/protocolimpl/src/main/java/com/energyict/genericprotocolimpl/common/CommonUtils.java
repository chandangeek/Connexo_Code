package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
//import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterGroup;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceType;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.coreimpl.DeviceFactoryImpl;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.metadata.Criterium;
import com.energyict.metadata.TypeDescriptor;
import com.energyict.protocol.InvalidPropertyException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author gna
 */
public final class CommonUtils {

    /**
     * Find an Device by it's serialNumber in the database. The serialnumber is not unique so if multiples were found, exceptions will be thrown.
     * If no rtu was found we will try to create one using the given DeviceType. If the folderExtNameProperty is entered, then the new Device will be
     * placed in that folder
     *
     * @param serialNumber          - the SerialNumber of the Device
     * @param rtuTypeProperty       - the Prototype for the new Device if non was found
     * @param folderExtNameProperty - the external name of the folder to place the new Device
     * @return an Device
     * @throws IOException       if multiple meters were found in the database
     * @throws SQLException      if database exception occurred
     * @throws BusinessException if business exception occurred
     */
    public static Device findOrCreateDeviceBySerialNumber(String serialNumber, String rtuTypeProperty, String folderExtNameProperty) throws IOException, SQLException, BusinessException {
        List result = mw().getDeviceFactory().findBySerialNumber(serialNumber);
        if (result.size() == 1) {        // we found the rtu so return it
            return (Device) result.get(0);
        } else if (result.size() > 1) {
            throw new IOException("Multple meters found in database with serialnumber " + serialNumber);
        } else {                    // no results were found, try to create it
            DeviceType rtuType = getRtuType(rtuTypeProperty);
            return createMeterWithSerialNumber(rtuType, serialNumber, folderExtNameProperty);
        }
    }

    /**
     * Find an Device by it's serialNumber in the database. The serialnumber is not unique so if multiples were found, exceptions will be thrown.
     * If no rtu was found we will return 'null'
     *
     * @param serialNumber - the SerialNumber of the Device
     * @return an Device
     * @throws IOException       if multiple meters were found in the database
     * @throws SQLException      if database exception occurred
     * @throws BusinessException if business exception occurred
     */
    public static Device findDeviceBySerialNumber(String serialNumber) throws IOException, SQLException, BusinessException {
        List result = mw().getDeviceFactory().findBySerialNumber(serialNumber);
        if (result.size() == 1) {        // we found the rtu so return it
            return (Device) result.get(0);
        } else if (result.size() > 1) {
            throw new IOException("Multple meters found in database with serialnumber " + serialNumber);
        } else {
            return null;
        }
    }

    /**
     * Find an Device by it's deviceId in the database. The deviceId is not unique so if multiples were found, exceptions will be thrown.
     * If no rtu was found we will try to create one using the given DeviceType. If the folderExtNameProperty is entered, then the new Device will be
     * placed in that folder
     *
     * @param deviceId              - the DeviceId of the Device
     * @param rtuTypeProperty       - the Prototype for the new Device if non was found
     * @param folderExtNameProperty - the external name of the folder to place the new Device
     * @return an Device
     * @throws IOException       if multiple meters were found in the database
     * @throws SQLException      if database exception occurred
     * @throws BusinessException if business exception occurred
     */
    public static Device findOrCreateDeviceByDeviceId(String deviceId, String rtuTypeProperty, String folderExtNameProperty) throws IOException, SQLException, BusinessException {
        List result = mw().getDeviceFactory().findByDeviceId(deviceId);
        if (result.size() == 1) {        // we found the rtu so return it
            return (Device) result.get(0);
        } else if (result.size() > 1) {
            throw new IOException("Multple meters found in database with deviceId " + deviceId);
        } else {                    // no results were found, try to create it
            DeviceType rtuType = getRtuType(rtuTypeProperty);
            return createMeterWithDeviceId(rtuType, deviceId, folderExtNameProperty);
        }
    }

    /**
     * Create a new Device an place it in the given folder
     * <p/>
     * After creation we iterate over all the CommunicationSchedulers
     * of the device and set the next reading date to now, when the
     * auto reschedule is enabled and the next reading date is NULL.
     *
     * @param rtuType               - the {@link com.energyict.mdw.core.DeviceType} for the new Device
     * @param serialNumber          - the serialNumber for the new Device
     * @param folderExtNameProperty - the folder to place the new Device
     * @return a new Device
     * @throws SQLException      if database exception occurred
     * @throws BusinessException if business exception occurred
     */
    public static Device createMeterWithSerialNumber(DeviceType rtuType, String serialNumber, String folderExtNameProperty) throws SQLException, BusinessException {
        DeviceShadow shadow = rtuType.getConfigurations().get(0).newDeviceShadow();
        shadow.setName(serialNumber);
        shadow.setSerialNumber(serialNumber);

        if (folderExtNameProperty != null) {
            Folder result = mw().getFolderFactory().findByExternalName(folderExtNameProperty);
            if (result != null) {
                shadow.setFolderId(result.getId());
            } // else the new rtu will be placed in the prototype folder
        }// else the new rtu will be placed in the prototype folder

        Device rtu = mw().getDeviceFactory().create(shadow);
        setNextCommunications(rtu);
        return rtu;
    }

    /**
     * Create a new Device an place it in the given folder.
     * <p/>
     * After creation we iterate over all the CommunicationSchedulers
     * of the device and set the next reading date to now, when the
     * auto reschedule is enabled and the next reading date is NULL.
     *
     * @param rtuType               - the {@link com.energyict.mdw.core.DeviceType} for the new Device
     * @param deviceId              - the deviceId for the new Device
     * @param folderExtNameProperty - the folder to place the new Device
     * @return a new Device
     * @throws SQLException      if database exception occurred
     * @throws BusinessException if business exception occurred
     */
    public static Device createMeterWithDeviceId(DeviceType rtuType, String deviceId, String folderExtNameProperty) throws SQLException, BusinessException {
        DeviceShadow shadow = rtuType.getConfigurations().get(0).newDeviceShadow();
        shadow.setName("Device - " + deviceId);
//        shadow.setDeviceId(deviceId);

        if (folderExtNameProperty != null) {
            Folder result = mw().getFolderFactory().findByExternalName(folderExtNameProperty);
            if (result != null) {
                shadow.setFolderId(result.getId());
            } // else the new rtu will be placed in the prototype folder
        }// else the new rtu will be placed in the prototype folder

        Device rtu = mw().getDeviceFactory().create(shadow);
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
    private static void setNextCommunications(Device slaveDevice) throws SQLException, BusinessException {
//        List<CommunicationScheduler> schedulers = slaveDevice.getCommunicationSchedulers();
//        for (CommunicationScheduler scheduler : schedulers) {
//            if (scheduler.getActive() && (scheduler.getNextCommunication() == null)) {
//                CommunicationSchedulerShadow shadow = scheduler.getShadow();
//                shadow.setNextCommunication(new Date());
//                scheduler.update(shadow);
//            }
//        }
    }

    /**
     * Find the DeviceType for the given RtuTypeName
     *
     * @param rtuTypeProperty - the name of the DeviceType
     * @return the found rtuType
     * @throws InvalidPropertyException when the name of the rtuType is null, when no DeviceType was found for the given name and when
     *                                  the found DeviceType doesn't have a prototype Device
     */
    public static DeviceType getRtuType(String rtuTypeProperty) throws InvalidPropertyException {
        if (rtuTypeProperty != null) {
            DeviceType rtuType = mw().getDeviceTypeFactory().find(rtuTypeProperty);
            if (rtuType == null) {
                throw new InvalidPropertyException("No rtutype defined with name '" + rtuTypeProperty + "'.");
            } else if (rtuType.getPrototypeDevice() == null) {
                throw new InvalidPropertyException("Rtutype '" + rtuTypeProperty + "' has no prototype rtu.");
            } else {
                return rtuType;
            }
        } else {
            throw new InvalidPropertyException("No automatic meter creation: no property DeviceType defined.");
        }
    }

    /**
     * Short notation for the current MeteringWarehous
     */
    public static MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
    }

    /**
     * Find a Device for the given phoneNumber
     *
     * @param phoneNumber the device phoneNumber as String
     * @return one single Device
     * @throws IOException when no device or more then one device was found with the given phoneNumber
     */
    public static Device findDeviceByPhoneNumber(String phoneNumber) throws IOException {
        DeviceFactoryImpl factory = (DeviceFactoryImpl) mw().getDeviceFactory();
        TypeDescriptor type = factory.getTypeDescriptor();
        com.energyict.metadata.SearchFilter filter = new com.energyict.metadata.SearchFilter(type);
        filter.addAnd(Criterium.eq(type.getAttributeDescriptor("phoneNumber"), phoneNumber));
        List<Device> result = factory.findBySearchFilter(filter);
        if (result.size() == 1) {
            return result.get(0);
        } else if (result.size() > 1) {
            throw new IOException("Multiple meters found in database with phoneNumber " + phoneNumber);
        } else {
            throw new IOException("No meter found in database with phoneNumber " + phoneNumber);
        }
    }

    /**
     * Check whether the {@link com.energyict.mdw.amr.RegisterGroup} from the given Register is in the given RegisterGroup-List
     *
     * @param groups - a List of {@link com.energyict.mdw.amr.RegisterGroup}s
     * @param rr     - the {@link com.energyict.mdw.amr.Register} to check
     * @return true if the list contains the RegisterGroup from the given Register, false otherwise
     */
    public static boolean isInRegisterGroup(List<RegisterGroup> groups, Register rr) {
        if (rr.getGroup() == null) {
            if (groups.size() == 0) {
                return true;
            }
            return false;
        }
        Iterator<RegisterGroup> it = groups.iterator();
        while (it.hasNext()) {
            if (rr.getGroup().equals(it.next())) {
                return true;
            }
        }
        return false;
    }
}
