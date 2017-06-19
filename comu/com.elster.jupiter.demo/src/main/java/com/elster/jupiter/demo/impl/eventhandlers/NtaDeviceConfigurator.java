/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.eventhandlers;


import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * {@LocalEvent} handler that can be used at the import of devices so to create an adequate NTA Device to be used with the (NTA) simulation tool
 * The com.elster.jupiter.demo.device.import.handler component must be enabled in the container's system console (components)!! to
 * be effective
 */

@Component(name = "com.elster.jupiter.demo.device.import.handler", service = Subscriber.class, immediate = true, enabled = false )
public class NtaDeviceConfigurator extends EventHandler<LocalEvent> {

    private static final String UPDATE_DEVICE_TOPIC = "com/energyict/mdc/device/data/device/UPDATED";
    private static final String NTA_SIM_TOOL_TABLE_NAME = "NTADEVICE";
    private static final String CREATE_NTA_SIM_TOOL_RECORD = "INSERT INTO %s (ID, SERIAL_NUMBER, MASTER_KEY, AUTHENTICATION_KEY, ENCRYPTION_KEY, SECRET, CORE_FIRMWARE_VERSION, CORE_IMAGE_IDENTIFIER, MODULE_FIRMWARE_VERSION, MODULE_VERSION_IDENTIFIER, NUMBER_OF_MBUS_DEVICES, E_CONTROL_MODE, ACTIVITY_CALENDAR_NAME) VALUES (?,?,'31313232333334343535363637373838', '31313232333334343535363637373838', '31313232333334343535363637373838','ntaSim','V1.0.0.1','V1.0.0.2','NTA-Sim_V_1.0.0','V1.0.0.4','0', '2', 'Re-Cu-01')";
    private static final String FIND_NTA_SIM_TOOL_RECORD = "SELECT COUNT(ID) FROM %1s WHERE SERIAL_NUMBER = '%2s'";
    private volatile OrmService ormService;

    private Device currentDevice;

    public NtaDeviceConfigurator() {
        super(LocalEvent.class);
    }

    @Reference
    public void setOrmService(OrmService ormService){
        this.ormService = ormService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        // preferred to do this at creation time: unfortunately the importer does not create a device with the
        // serialnumber. The serial number is set afterwards...
        if (UPDATE_DEVICE_TOPIC.equals(event.getType().getTopic())) {
            Device device = (Device) event.getSource();
            configureNTADevice(device);
        }
    }

    private void configureNTADevice(Device device){
        if (device != currentDevice) {
            currentDevice = device;    // to avoid that setting the ntaSimulationTool property will recall this...
            Optional<DataModel> dataModel = this.ormService.getDataModel(DeviceDataServices.COMPONENT_NAME);
            if (device.getSerialNumber() != null && !device.getSerialNumber().isEmpty() && dataModel.isPresent()) {
                try (Connection connection = dataModel.get().getConnection(false)) {
                    try (PreparedStatement findStatement = connection.prepareStatement(String.format(FIND_NTA_SIM_TOOL_RECORD, NTA_SIM_TOOL_TABLE_NAME, device.getSerialNumber()));
                         PreparedStatement insertStatement = connection.prepareStatement(String.format(CREATE_NTA_SIM_TOOL_RECORD, NTA_SIM_TOOL_TABLE_NAME))) {
                        ResultSet count = findStatement.executeQuery();
                        if (count.next() && count.getInt(1) == 0) {
                            insertStatement.setLong(1, getNext(connection, NTA_SIM_TOOL_TABLE_NAME + "ID"));
                            insertStatement.setString(2, device.getSerialNumber());
                            insertStatement.executeUpdate();
                        }
                        device.setProtocolProperty("NTASimulationTool", true);
                        device.save();
                        currentDevice = null;
                    }
                } catch (SQLException e) {
                    throw new UnableToCreate("Unable to execute native sql command for NTA tool configuration: " + e.getMessage());
                }
            }
        }
    }

    private long getNext(Connection connection, String sequence) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}