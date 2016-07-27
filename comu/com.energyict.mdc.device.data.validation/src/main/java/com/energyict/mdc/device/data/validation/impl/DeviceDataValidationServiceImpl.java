package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.kpi.DataValidationKpiScore;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.energyict.mdc.device.data.validation.DeviceValidationKpiResults;
import com.energyict.mdc.device.data.validation.ValidationOverview;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by dragos on 7/21/2015.
 */
@Component(name = "com.energyict.mdc.device.data.validation", service = {DeviceDataValidationService.class}, property = "name=" + DeviceDataValidationService.COMPONENT_NAME, immediate = true)
public class DeviceDataValidationServiceImpl implements DeviceDataValidationService {

    private volatile ValidationService validationService;
    private volatile DataModel dataModel;


    // For OSGi purposes
    public DeviceDataValidationServiceImpl() {
    }

    // For Testing purposes
    @Inject
    DeviceDataValidationServiceImpl(ValidationService validationService, OrmService ormService) {
        this();
        this.setValidationService(validationService);
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
    public List<ValidationOverview> getValidationResultsOfDeviceGroup(long groupId, Range<Instant> range) {
        List<ValidationOverview> list = new ArrayList<>();
        List<Long> deviceIds = validationService.getDevicesWithSuspects(groupId);
        SqlBuilder validationOverviewBuilder = new SqlBuilder();
        validationOverviewBuilder.append("SELECT DEV.mrid, DEV.serialnumber, DT.name, DC.name FROM DDC_DEVICE DEV ");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICETYPE DT ON dev.devicetype = DT.id");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICECONFIG DC ON dev.deviceconfigid = DC.id");
        validationOverviewBuilder.append(" WHERE DEV.id IN (");
        validationOverviewBuilder.append(deviceIds.isEmpty() ? "0" : deviceIds.stream().sorted().map(id -> String.valueOf(id)).collect(Collectors.joining(", ")));
        validationOverviewBuilder.append(")");
        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement statement = validationOverviewBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.getFetchSize();
                while (resultSet.next()) {
                    AtomicLong idx = new AtomicLong(0);
                    list.add(new ValidationOverviewImpl(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            new DeviceValidationKpiResults(
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getTotalSuspects().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getChannelSuspects().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getRegisterSuspects().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getAllDataValidated().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getTimestamp(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getThresholdValidator().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getMissingValuesValidator().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getReadingQualitiesValidator().longValue(),
                                    getKpiScores(groupId,deviceIds.get(idx.intValue()),range).getRegisterIncreaseValidator().longValue()
                            )));
                    idx.incrementAndGet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

   private DataValidationKpiScore  getKpiScores(long groupId, long deviceId, Range<Instant> interval){
       return validationService.getDataValidationKpiScores(groupId,deviceId,interval)
               .orElseThrow(() -> new IllegalArgumentException("No Score could be found for end device having ID = : " + deviceId +" belonging to the group with ID = " + groupId));
    }
}
