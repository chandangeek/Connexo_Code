/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

import static com.elster.jupiter.orm.Version.version;

public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    private final FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller;

    @Inject
    UpgraderV10_7(DataModel dataModel, FirmwareCampaignServiceCallLifeCycleInstaller firmwareCampaignServiceCallLifeCycleInstaller) {
        this.dataModel = dataModel;
        this.firmwareCampaignServiceCallLifeCycleInstaller = firmwareCampaignServiceCallLifeCycleInstaller;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        firmwareCampaignServiceCallLifeCycleInstaller.createServiceCallTypes();
        dataModelUpgrader.upgrade(dataModel, version(10, 7));
        //    serviceCallTable();
    }

    private void serviceCallTable() {
//        executeQuery(dataModel,"", this::convertResultSetToMap)
//                .forEach(firCamp -> execute(dataModel,""));
//        execute(dataModel,
//            "INSERT INTO SCS_SERVICE_CALL (id, parent, state, servicecalltype, versioncount, createtime, modtime, username)"
//                    +"SELECT (SELECT NVL(MAX(ID),0) FROM SCS_SERVICE_CALL)+1 campaign_name, device_type FROM FWC_CAMPAIGN");
    }

    private void firmwareCampaignTable() {
//        execute(dataModel,
//                "INSERT INTO " + FirmwareCampaignPersistenceSupport.TABLE_NAME + "(name, device_type)"
//                +"SELECT campaign_name, device_type FROM FWC_CAMPAIGN");
    }

//    private <T> T executeQuery(DataModel dataModel, String sql, SqlExceptionThrowingFunction<ResultSet, T> resultMapper) {
//        try (Connection connection = dataModel.getConnection(false);
//             Statement statement = connection.createStatement()) {
//            return executeQuery(statement, sql, resultMapper);
//        } catch (SQLException e) {
//            throw new UnderlyingSQLFailedException(e);
//        }
//    }

//    private List<FirCamp> convertResultSetToMap(ResultSet resultSet) throws SQLException {
//        List<FirCamp> map = new ArrayList<>();
//        while (resultSet.next()) {
//            map.put(resultSet.getLong(1), resultSet.getLong(2));
//        }
//        return map;
//    }
//
//    private class FirCamp{
//        List<FirCampItem> list;
//        int id;
//
//    }
//
//    private class FirCampItem{
//        int i;
//
//    }

}
