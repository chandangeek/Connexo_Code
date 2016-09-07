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
import javax.validation.constraints.DecimalMax;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

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
        List<Long> deviceIds  = validationService.getDevicesWithSuspects(groupId);
        SqlBuilder validationOverviewBuilder = new SqlBuilder();
        validationOverviewBuilder.append("SELECT DEV.mrid, DEV.serialnumber, DT.name, DC.name FROM DDC_DEVICE DEV ");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICETYPE DT ON dev.devicetype = DT.id");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICECONFIG DC ON dev.deviceconfigid = DC.id");
        whereClause(validationOverviewBuilder, deviceIds);
        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement statement = validationOverviewBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                AtomicLong idx = new AtomicLong(0);
                while (resultSet.next() && getKpiScores(groupId, deviceIds.get(idx.intValue()), range).isPresent()) {
                    list.add(new ValidationOverviewImpl(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            new DeviceValidationKpiResults(
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getTotalSuspects().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getChannelSuspects().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getRegisterSuspects().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getAllDataValidated().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getTimestamp(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getThresholdValidator().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getMissingValuesValidator().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getReadingQualitiesValidator().longValue(),
                                    getKpiScores(groupId, deviceIds.get(idx.intValue()), range).get().getRegisterIncreaseValidator().longValue()
                            )));
                    idx.incrementAndGet();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Optional<DataValidationKpiScore> getKpiScores(long groupId, long deviceId, Range<Instant> interval) {
        return validationService.getDataValidationKpiScores(groupId, deviceId, interval);

    }

    private SqlBuilder whereClause(SqlBuilder sqlBuilder, List<Long> deviceIds) {
        List<List<Long>> lists = splitListToSublists(deviceIds).collect(Collectors.toList());
        if (lists.size() <= 1) {
            sqlBuilder.append(" WHERE DEV.id IN (");
            sqlBuilder.append(lists.size() == 0 ? "0" : lists.stream().sorted().map(id -> String.valueOf(id)).collect(Collectors.joining(", ")));
            sqlBuilder.append(")");
        } else {
            List<Long> lastElement = lists.get(lists.size() - 1);
            sqlBuilder.append(" WHERE DEV.id IN (");
            lists.forEach(list -> {
                if (!lastElement.equals(list)) {
                    sqlBuilder.append(list.stream().sorted().map(id -> String.valueOf(id)).collect(Collectors.joining(", ")));
                    sqlBuilder.append(")");
                    sqlBuilder.append(" OR DEV.id IN (");
                } else {
                    sqlBuilder.append(list.stream().sorted().map(id -> String.valueOf(id)).collect(Collectors.joining(", ")));
                    sqlBuilder.append(")");
                }
            });
        }
        return sqlBuilder;
    }

    private Stream<List<Long>> splitListToSublists(List<Long> deviceIds) {
        int acceptedSize = 999;
        int listSize = deviceIds.size();
        if (listSize <= 0) {
            return Stream.empty();
        }
        int subLists = (listSize - 1) / acceptedSize;
        return IntStream.range(0, subLists + 1).mapToObj(
                nb -> deviceIds.subList(nb * acceptedSize, nb == subLists ? listSize : (nb + 1) * acceptedSize));
    }

}
