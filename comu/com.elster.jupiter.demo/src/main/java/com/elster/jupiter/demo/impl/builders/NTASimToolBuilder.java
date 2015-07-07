package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.util.ArrayList;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class NTASimToolBuilder implements Builder<Void> {
    private static final String NTA_SIM_TOOL_TABLE_NAME = "NTADEVICE";
    private static final String CREATE_NTA_SIM_TOOL_TABLE = "CREATE TABLE "+NTA_SIM_TOOL_TABLE_NAME+" (ID NUMBER(10, 0) NOT NULL PRIMARY KEY, SERIAL_NUMBER VARCHAR2(255) NOT NULL, MASTER_KEY VARCHAR2(40) NOT NULL, SECRET VARCHAR2(16) NOT NULL, AUTHENTICATION_KEY VARCHAR2(40) NOT NULL, ENCRYPTION_KEY VARCHAR2(40) NOT NULL, CORE_FIRMWARE_VERSION VARCHAR2(48) NOT NULL, CORE_IMAGE_IDENTIFIER VARCHAR2(48) NOT NULL, MODULE_FIRMWARE_VERSION VARCHAR2(48) NOT NULL, MODULE_VERSION_IDENTIFIER VARCHAR2(48) NOT NULL, ACTIVITY_CALENDAR_NAME VARCHAR2(255) DEFAULT 0 NOT NULL, P2_ENCRYPTION_STATUS NUMBER(1, 0) DEFAULT 0 NOT NULL, E_CONTROL_STATE NUMBER(1, 0) DEFAULT 1 NOT NULL, E_CONTROL_MODE NUMBER(1, 0) DEFAULT 0 NOT NULL, NUMBER_OF_MBUS_DEVICES NUMBER(1, 0) DEFAULT 1 NOT NULL, G_CONTROL_STATE NUMBER(1, 0) DEFAULT 1 NOT NULL, G_CONTROL_MODE NUMBER(1, 0) DEFAULT 0 NOT NULL, ADMINISTRATIVE_STATUS NUMBER(1,0) DEFAULT 0, P2_ENCRYPTION_KEY_STATUS NUMBER(1,0) DEFAULT 0, CONFIGURATION_FLAGS NUMBER(10,0) DEFAULT 0)";
    private static final String CREATE_NTA_SIM_TOOL_INDEX = "CREATE UNIQUE INDEX "+NTA_SIM_TOOL_TABLE_NAME+"_INDEX_SERIAL_NUMBER ON "+NTA_SIM_TOOL_TABLE_NAME+" (SERIAL_NUMBER ASC)";
    private static final String CREATE_NTA_SIM_TOOL_SEQUENCE = "CREATE SEQUENCE "+NTA_SIM_TOOL_TABLE_NAME+"ID START WITH 1 CACHE 1000";
    private static final String CREATE_NTA_SIM_TOOL_RECORD = "INSERT INTO "+NTA_SIM_TOOL_TABLE_NAME+" (ID, SERIAL_NUMBER, MASTER_KEY, AUTHENTICATION_KEY, ENCRYPTION_KEY, SECRET, CORE_FIRMWARE_VERSION, CORE_IMAGE_IDENTIFIER, MODULE_FIRMWARE_VERSION, MODULE_VERSION_IDENTIFIER, NUMBER_OF_MBUS_DEVICES) VALUES (?,?,'31313232333334343535363637373838', '31313232333334343535363637373838', '31313232333334343535363637373838','ntaSim','V1.0.0.1','V1.0.0.2','V1.0.0.3','V1.0.0.4','0')";
    private static final String DROP_NTA_SIM_TOOL_TABLE = "DROP TABLE "+NTA_SIM_TOOL_TABLE_NAME;
    private static final String DROP_NTA_SIM_TOOL_SEQUENCE = "DROP SEQUENCE  "+NTA_SIM_TOOL_TABLE_NAME + "ID";
    private static final String DISABLE_COMTASK_RETRY = "update CTS_COMTASK SET maxnroftries = 1";

    private final DataModel dataModel;
    private final DeviceService deviceService;

    @Inject
    public NTASimToolBuilder(DataModel dataModel, DeviceService deviceService) {
        this.dataModel = dataModel;
        this.deviceService = deviceService;
    }

    @Override
    public Void get() {
        return create();
    }

    @Override
    public Optional<Void> find() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Void create() {
        System.out.println(" ==> Executing the NTA Sim config");

        List<String> serialNumbers = new ArrayList<>();
        int start=0;
        List<String> newSerialNumbers = new ArrayList<>();
        newSerialNumbers.add("sentinal");
        while(!newSerialNumbers.isEmpty()) {
            newSerialNumbers = deviceService.findAllDevices(where("mRID").like(Constants.Device.STANDARD_PREFIX + "*")).paged(start, 1000)
                    .stream().map(Device::getSerialNumber).collect(Collectors.toList());
            start+=1000;
            serialNumbers.addAll(newSerialNumbers);
        }
        try (Connection connection = dataModel.getConnection(false)){
            connection.prepareStatement(DISABLE_COMTASK_RETRY).execute();
            createNtaSimTable(connection);
            PreparedStatement statement = connection.prepareStatement(CREATE_NTA_SIM_TOOL_RECORD);
            long id = getNext(connection, NTA_SIM_TOOL_TABLE_NAME + "ID");
            for (String serialNumber : serialNumbers) {
                statement.setLong(1, id++);
                statement.setString(2, serialNumber);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new UnableToCreate("Unable to execute native sql command for NTA tool configuration: " + e.getMessage());
        }
        return null;
    }

    private void createNtaSimTable(Connection connection) throws SQLException {
        try {
            connection.prepareStatement(DROP_NTA_SIM_TOOL_TABLE).execute();
            connection.prepareStatement(DROP_NTA_SIM_TOOL_SEQUENCE).execute();
        } catch(SQLException ex){
            // ignore it
        }
        connection.prepareStatement(CREATE_NTA_SIM_TOOL_TABLE).execute();
        connection.prepareStatement(CREATE_NTA_SIM_TOOL_INDEX).execute();
        connection.prepareStatement(CREATE_NTA_SIM_TOOL_SEQUENCE).execute();
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
