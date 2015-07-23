package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.energyict.mdc.device.data.validation.ValidationOverview;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by dragos on 7/21/2015.
 */
@Component(name = "com.energyict.mdc.device.data.validation", service = {DeviceDataValidationService.class}, property = "name=" + DeviceDataValidationService.COMPONENT_NAME, immediate = true)
public class DeviceDataValidationServiceImpl implements DeviceDataValidationService {

    private volatile ValidationService validationService;
    private volatile DataModel dataModel;

    public DeviceDataValidationServiceImpl() {
    }

    @Inject
    DeviceDataValidationServiceImpl(ValidationService validationService, OrmService ormService) {
        this.validationService = validationService;
        setOrmService(ormService);
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.getDataModel(ValidationService.COMPONENTNAME).orElse(null);
    }

    @Override
    public List<ValidationOverview> getValidationResultsOfDeviceGroup(Long groupId, Optional<Integer> start, Optional<Integer> limit) {
        List<ValidationOverview> list = new ArrayList<>();
        Optional<SqlBuilder> found = validationService.getValidationResults(groupId, start, limit);
        if(found.isPresent()){
            SqlBuilder sqlBuilder = found.get();
            sqlBuilder.insertAt(0,
                    "SELECT DEV.mrid, DEV.serialnumber, DT.name, DC.name FROM DDC_DEVICE DEV " +
                    "LEFT JOIN MTR_ENDDEVICE ED ON (DEV.id=ed.amrid) " +
                    "LEFT JOIN DTC_DEVICETYPE DT ON (dev.devicetype=DT.id) " +
                    "LEFT JOIN DTC_DEVICECONFIG DC ON (dev.deviceconfigid=DC.id) " +
                    "WHERE " +
                        "(ED.AMRSYSTEMID = " + KnownAmrSystem.MDC.getId() + "  AND (ED.amrid) IN (");
            sqlBuilder.append("))");

            try (PreparedStatement statement = sqlBuilder.prepare(dataModel.getConnection(false))) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        list.add(new ValidationOverview(
                                        resultSet.getString(1),
                                        resultSet.getString(2),
                                        resultSet.getString(3),
                                        resultSet.getString(4)));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}
