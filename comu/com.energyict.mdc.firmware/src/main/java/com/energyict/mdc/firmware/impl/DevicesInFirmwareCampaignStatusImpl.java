/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@LiteralSql
public class DevicesInFirmwareCampaignStatusImpl{

    public Map<String, Long> getStatusMap() {
        // Order of this statuses is important for FE
        Map<String, Long> result = new LinkedHashMap<>();
        result.put(FirmwareManagementDeviceStatus.Constants.SUCCESS, this.success);
        result.put(FirmwareManagementDeviceStatus.Constants.FAILED, this.failed);
        result.put(FirmwareManagementDeviceStatus.Constants.CONFIGURATION_ERROR, this.configurationError);
        result.put(FirmwareManagementDeviceStatus.Constants.ONGOING, this.ongoing);
        result.put(FirmwareManagementDeviceStatus.Constants.PENDING, this.pending);
        result.put(FirmwareManagementDeviceStatus.Constants.CANCELLED, this.cancelled);
        return result;
    }

    public enum Fields {
        CAMPAIGN("campaign"),
        STATUS_SUCCESS("success"),
        STATUS_FAILED("failed"),
        STATUS_ONGOING("ongoing"),
        STATUS_PENDING("pending"),
        STATUS_CONFIGURATION_ERROR("configurationError"),
        STATUS_CANCELLED("cancelled"),;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareCampaign> campaign = ValueReference.absent();

    private long success = 0;
    private long failed = 0;
    private long ongoing = 0;
    private long pending = 0;
    private long configurationError = 0;
    private long cancelled = 0;

    private final DataModel dataModel;

    @Inject
    public DevicesInFirmwareCampaignStatusImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DevicesInFirmwareCampaignStatusImpl init(FirmwareCampaign campaign) {
        this.campaign.set(campaign);
        return this;
    }

    public void update() {
        try (Connection conn = dataModel.getConnection(false)) {
            Map<String, String> statusSumQuery = getDevicesInFirmwareCampaignStatusSumQuery();
            PreparedStatement groupingStatement = buildSQL(statusSumQuery).prepare(conn);
            try (ResultSet rs = groupingStatement.executeQuery()) {
                if (rs.next()) {
                    this.success = rs.getLong(FirmwareManagementDeviceStatus.Constants.SUCCESS.toUpperCase());
                    this.failed = rs.getLong(FirmwareManagementDeviceStatus.Constants.FAILED.toUpperCase());
                    this.ongoing = rs.getLong(FirmwareManagementDeviceStatus.Constants.ONGOING.toUpperCase());
                    this.pending = rs.getLong(FirmwareManagementDeviceStatus.Constants.PENDING.toUpperCase());
                    this.configurationError = rs.getLong(FirmwareManagementDeviceStatus.Constants.CONFIGURATION_ERROR.toUpperCase());
                    this.cancelled = rs.getLong(FirmwareManagementDeviceStatus.Constants.CANCELLED.toUpperCase());
                    dataModel.update(this);
                }
            }
        } catch (SQLException sqlEx) {
            long campaignId = campaign.isPresent() ? campaign.get().getId() : 0;
            Logger.getLogger(DevicesInFirmwareCampaignStatusImpl.class.getSimpleName())
                    .warning("Unable to update a device statistic for firmware campaign (id = " + campaignId + "), reason: " + sqlEx.getMessage());
            sqlEx.printStackTrace();
        }
    }

    public SqlBuilder buildSQL(Map<String, String> statusQueryMap) {
        SqlBuilder builder = new SqlBuilder();
        String campaign = "CAMPAIGN";
        builder.append(" SELECT " + campaign + ", ");
        String statuses = statusQueryMap.values().stream().collect(Collectors.joining(", "));
        builder.append(statuses);
        builder.append(" FROM " + TableSpecs.FWC_CAMPAIGN_DEVICES.name());
        builder.append(" WHERE " + campaign + " = " + this.campaign.get().getId());
        builder.append(" GROUP BY " + campaign);
        return builder;
    }

    private Map<String, String> getDevicesInFirmwareCampaignStatusSumQuery() {
        Map<String, List<String>> statusMapping = getDeviceInFirmwareCampaignStatusMapping();
        Map<String, String> statusQuery = new HashMap<>();
        for (Map.Entry<String, List<String>> mapping : statusMapping.entrySet()) {
            statusQuery.put(mapping.getKey(), mapping.getValue().stream()
                    .collect(Collectors.joining(" OR " + DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName() + "=",
                            "SUM(CASE WHEN " + DeviceInFirmwareCampaignImpl.Fields.STATUS.fieldName() + "=",
                            " THEN 1 ELSE 0 END) AS " + mapping.getKey())));
        }
        return statusQuery;
// results into
//        {"success","SUM(CASE WHEN STATUS = 3 OR STATUS = 4 OR STATUS = 5 OR STATUS = 6 OR STATUS = 7 OR STATUS = 8 OR STATUS = 9 OR STATUS = 10 OR STATUS = 11 THEN 1 ELSE 0 END) AS success"},
//        {"failed", "SUM(CASE WHEN STATUS = 2 THEN 1 ELSE 0 END) AS failed"},
//        {"ongoing", "SUM(CASE WHEN STATUS = 1 THEN 1 ELSE 0 END) AS ongoing"},
//        {"pending"; "SUM(CASE WHEN STATUS = 0 THEN 1 ELSE 0 END) AS pending"},
//        {"configurationError", "SUM(CASE WHEN STATUS = 12 THEN 1 ELSE 0 END) AS configurationError"},
//        {"cancelled", "SUM(CASE WHEN STATUS = 13 THEN 1 ELSE 0 END) AS cancelled"}

    }

    private Map<String, List<String>> getDeviceInFirmwareCampaignStatusMapping() {
        Map<String, List<String>> statusMapping = new HashMap<>();
        for (FirmwareManagementDeviceStatus status : FirmwareManagementDeviceStatus.values()) {
            List<String> statusOrdinals = statusMapping.get(status.key());
            if (statusOrdinals == null) {
                statusOrdinals = new ArrayList<>();
                statusMapping.put(status.key(), statusOrdinals);
            }
            statusOrdinals.add(String.valueOf(status.ordinal()));
        }
        return statusMapping;
// results into
//        {"success",[UPLOAD_SUCCESS (3), ACTIVATION_PENDING (4), ACTIVATION_ONGOING (5), ACTIVATION_FAILED (6), ACTIVATION_SUCCESS(7), VERIFICATION_ONGOING (8), VERIFICATION_TASK_FAILED (9), VERIFICATION_FAILED (10), VERIFICATION_SUCCESS (11)},
//        {"failed",[UPLOAD_FAILED (2)]},
//        {"ongoing", [UPLOAD_ONGOING (1)]},
//        {"pending";[ UPLOAD_PENDING (0)]},
//        {"configurationError", [CONFIGURATION_ERROR (12)]},
//        {"cancelled", [CONFIGURATION_ERROR (13)]}

    }

    public long getSuccess() {
        return success;
    }

    public long getFailed() {
        return failed;
    }

    public long getOngoing() {
        return ongoing;
    }

    public long getPending() {
        return pending;
    }

    public long getConfigurationError() {
        return configurationError;
    }

    public long getCancelled() {
        return cancelled;
    }
}
