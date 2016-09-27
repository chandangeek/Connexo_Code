package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Pair;
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
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        Map<Long, DataValidationKpiScore> devicesWithScores = validationService.getDevicesIdsList(groupId)
                .stream()
                .map(deviceId -> Pair.of(deviceId, getKpiScores(groupId,deviceId,range)))
                .filter(pair -> pair.getLast().isPresent())
                .collect(Collectors.toMap(
                        Pair::getFirst,
                        pair -> pair.getLast().get())
                );
        List<Long> deviceIds = new ArrayList<>(devicesWithScores.keySet());

        SqlBuilder validationOverviewBuilder = new SqlBuilder();
        validationOverviewBuilder.append("SELECT DEV.mrid, DEV.serialnumber, DT.name, DC.name, DEV.id FROM DDC_DEVICE DEV ");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICETYPE DT ON dev.devicetype = DT.id");
        validationOverviewBuilder.append("  LEFT JOIN DTC_DEVICECONFIG DC ON dev.deviceconfigid = DC.id");
        whereClause(validationOverviewBuilder, deviceIds);
        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement statement = validationOverviewBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    if (devicesWithScores.containsKey(resultSet.getLong(5))) {
                        DataValidationKpiScore scores = devicesWithScores.get(resultSet.getLong(5));
                            list.add(new ValidationOverviewImpl(
                                    resultSet.getString(1),
                                    resultSet.getString(2),
                                    resultSet.getString(3),
                                    resultSet.getString(4),
                                    new DeviceValidationKpiResults(
                                            scores.getTotalSuspects().longValue(),
                                            scores.getChannelSuspects().longValue(),
                                            scores.getRegisterSuspects().longValue(),
                                            scores.getAllDataValidated().longValue(),
                                            scores.getTimestamp(),
                                            scores.getThresholdValidator().longValue(),
                                            scores.getMissingValuesValidator().longValue(),
                                            scores.getReadingQualitiesValidator().longValue(),
                                            scores.getRegisterIncreaseValidator().longValue()
                                    )));
                    }
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
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
            sqlBuilder.append(lists.size() == 0 ? "0" : lists.stream()
                    .flatMap(list -> list.stream().sorted())
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
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
