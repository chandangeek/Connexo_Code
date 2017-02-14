/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.kpi.DataValidationKpiCalculatorHandlerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

@LiteralSql
public class UpgraderV10_2 implements Upgrader {

    private static final Version VERSION = version(10, 2);
    private final DataModel dataModel;
    private final MessageService messageService;

    @Inject
    public UpgraderV10_2(DataModel dataModel, MessageService messageService) {
        this.dataModel = dataModel;
        this.messageService = messageService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        List<String> sql = new ArrayList<>();

        try (Connection connection = dataModel.getConnection(false);
             PreparedStatement preparedStatement = connection.prepareStatement("select VALUE from VAL_VALIDATIONRULEPROPS where NAME = 'intervalFlags'");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String oldValues = resultSet.getString(1);
                String cimCodes = convertToCIMCodes(oldValues);
                sql.add("UPDATE VAL_VALIDATIONRULEPROPS SET VALUE = '" + cimCodes + "' where VALUE = '" + oldValues + "'");
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }

        sql.add("UPDATE VAL_VALIDATIONRULE SET IMPLEMENTATION = 'com.elster.jupiter.validators.impl.ReadingQualitiesValidator' where IMPLEMENTATION = 'com.elster.jupiter.validators.impl.IntervalStateValidator'");
        sql.add("UPDATE VAL_VALIDATIONRULEPROPS SET NAME = 'readingQualities' where NAME = 'intervalFlags'");
        sql.add("create or replace function utc2date(utcms number, tz varchar2) return timestamp with time zone deterministic is begin return from_tz(cast(date'1970-1-1' + (utcms/86400000) as timestamp),'UTC') at time zone tz; end;");

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });

        dataModelUpgrader.upgrade(dataModel, VERSION);
        this.upgradeSubscriberSpecs();

        createMessageHandler(DataValidationKpiCalculatorHandlerFactory.TASK_DESTINATION, TranslationKeys.KPICALCULATOR_DISPLAYNAME);
        sql.add("UPDATE VAL_VALIDATIONRULESET set quality_system=2 where quality_system is null");
        sql.add("UPDATE VAL_DATAVALIDATIONTASK set quality_system=2 where quality_system is null");

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private void createMessageHandler(String destinationName, TranslationKey subscriberName) {
        Optional<DestinationSpec> destinationSpecOptional = messageService.getDestinationSpec(destinationName);
        if (!destinationSpecOptional.isPresent()) {
            DestinationSpec queue = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get().createDestinationSpec(destinationName, 60);
            queue.activate();
            queue.subscribe(subscriberName, ValidationService.COMPONENTNAME, Layer.DOMAIN);
        }
    }

    private String convertToCIMCodes(String oldValues) {
        StringBuilder result = new StringBuilder();

        String[] oldIntervalFlagValues = oldValues.split(PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR);
        for (String oldIntervalFlagValue : oldIntervalFlagValues) {
            Optional<IntervalFlags> cimCode = IntervalFlags.fromString(oldIntervalFlagValue);
            if (cimCode.isPresent()) {
                if (result.length() > 0) {
                    result.append(PropertySpecBuilder.DEFAULT_MULTI_VALUE_SEPARATOR);
                }
                result.append(cimCode.get().getCimCode().getCimCode());
            }
        }
        return result.toString();
    }

    private void upgradeSubscriberSpecs() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            this.upgradeSubscriberSpecs(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private void upgradeSubscriberSpecs(Connection connection) {
        try (PreparedStatement statement = this.upgradeSubscriberSpecsStatement(connection)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement upgradeSubscriberSpecsStatement(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE MSG_SUBSCRIBERSPEC SET nls_component = ?, nls_layer = ? WHERE name in (?, ?)");
        statement.setString(1, ValidationService.COMPONENTNAME);
        statement.setString(2, Layer.DOMAIN.name());
        statement.setString(3, TranslationKeys.KPICALCULATOR_DISPLAYNAME.getKey());
        statement.setString(4, TranslationKeys.MESSAGE_SPEC_SUBSCRIBER.getKey());
        return statement;
    }

    /**
     * A mapping between the interval flag values that were persisted for the old interval flag validator,
     * and their reading qualities CIM codes.
     */
    private enum IntervalFlags {
        BADTIME("badTime", ProtocolReadingQualities.BADTIME),
        BATTERY_LOW("batteryLow", ProtocolReadingQualities.BATTERY_LOW),
        CONFIGURATIONCHANGE("configurationChange", ProtocolReadingQualities.CONFIGURATIONCHANGE),
        CORRUPTED("corrupted", ProtocolReadingQualities.CORRUPTED),
        DEVICE_ERROR("deviceError", ProtocolReadingQualities.DEVICE_ERROR),
        MISSING("missing", ProtocolReadingQualities.MISSING),
        MODIFIED("modified", ProtocolReadingQualities.MODIFIED),
        OTHER("other", ProtocolReadingQualities.OTHER),
        OVERFLOW("overflow", ProtocolReadingQualities.OVERFLOW),
        PHASEFAILURE("phaseFailure", ProtocolReadingQualities.PHASEFAILURE),
        POWERDOWN("powerDown", ProtocolReadingQualities.POWERDOWN),
        POWERUP("powerUp", ProtocolReadingQualities.POWERUP),
        WATCHDOGRESET("watchdogReset", ProtocolReadingQualities.WATCHDOGRESET),
        REVERSERUN("reverseRun", ProtocolReadingQualities.REVERSERUN),
        SHORTLONG("shortLong", ProtocolReadingQualities.SHORTLONG),
        TEST("test", ProtocolReadingQualities.TEST),;

        private final ProtocolReadingQualities cimCode;
        private final String intervalFlagValue;

        IntervalFlags(String intervalFlagValue, ProtocolReadingQualities cimCode) {
            this.intervalFlagValue = intervalFlagValue;
            this.cimCode = cimCode;
        }

        public static Optional<IntervalFlags> fromString(String selectedIntervalFlag) {
            for (IntervalFlags intervalFlag : values()) {
                if (intervalFlag.getIntervalFlagValue().equals(selectedIntervalFlag)) {
                    return Optional.of(intervalFlag);
                }
            }
            return Optional.empty();
        }

        public ProtocolReadingQualities getCimCode() {
            return cimCode;
        }

        public String getIntervalFlagValue() {
            return intervalFlagValue;
        }
    }
}
