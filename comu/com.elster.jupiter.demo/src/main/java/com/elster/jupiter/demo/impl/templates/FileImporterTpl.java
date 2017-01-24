package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 11:04
 */
public enum FileImporterTpl implements Template<ImportSchedule, ImportScheduleBuilder> {

    DEVICE_INSTALLATION_IMPORTER("Installation", "DeviceInstallationImporterFactory", "devicelifecycle", "installation") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_READINGS_IMPORTER("Import readings", "DeviceReadingsImporterFactory", "readings") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    CONNECTION_ATTRIBUTES_IMPORTER("Import connection attributes", "ConnectionAttributesImportFactory", "connectionattributes"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    SECURITY_ATTRIBUTES_IMPORTER("Import security attributes", "SecurityAttributesImportFactory", "securityattributes"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    DEVICE_ACTIVATION_DEACTIVATION_IMPORTER("Activation Deactivation", "DeviceActivationDeactivationImportFactory", "devicelifecycle", "activation deactivation"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_COMMISSIONING_IMPORTER("Commissioning", "DeviceCommissioningImportFactory", "devicelifecycle", "commissioning"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_DECOMMISSIONG_IMPORTER("Decommissioning", "DeviceDecommissioningImportFactory", "devicelifecycle", "decommissioning"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone",defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_REMOVE_IMPORTER("Remove", "DeviceRemoveImportFactory", "devicelifecycle", "remove"),
    DEVICE_SHIPMENT_IMPORTER("Shipment", "DeviceShipmentImporterFactory", "devicelifecycle", "shipment") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    CALENDAR_IMPORTER("Calendar", "CalendarImporterFactory", "Calendar") {
        @Override
        protected Map<String, Object> getImporterProperties() {
            return Collections.emptyMap();
        }
    };

    public static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final String DEFAULT_PROCESSING_DIRECTORY = "progress";
    private static final String DEFAULT_SUCCESS_DIRECTORY = "success";
    private static final String DEFAULT_FAILURE_DIRECTORY = "failure";
    private static final String DEFAULT_PATH_MATCHER = "*.csv";
    private static final PeriodicalScheduleExpression DEFAULT_SCHEDULED_EXPRESSION = PeriodicalScheduleExpression.every(1).minutes().at(0).build();

    private String importerName;
    private String factoryName;
    private Path importBasePath;

    FileImporterTpl(String importerName, String factoryName, String importBasePath, String... basePathNodes) {
        this.importerName = importerName;
        this.factoryName = factoryName;
        this.importBasePath = Paths.get(importBasePath, basePathNodes);
    }

    protected Map<String, Object> getImporterProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("DeviceDataFileImporterFactory.delimiter", ";");
        return properties;
    }

    @Override
    public Class<ImportScheduleBuilder> getBuilderClass() {
        return ImportScheduleBuilder.class;
    }

    @Override
    public ImportScheduleBuilder get(ImportScheduleBuilder builder) {
        builder.withName(importerName)
               .withFileImporterFactoryName(factoryName)
               .withPathMatcher(DEFAULT_PATH_MATCHER)
               .withScheduleExpression(DEFAULT_SCHEDULED_EXPRESSION)
               .withImportDirectory(importBasePath.toString())
               .withInProcessDirectory(importBasePath.resolve(DEFAULT_PROCESSING_DIRECTORY).toString())
               .withSuccessDirectory(importBasePath.resolve(DEFAULT_SUCCESS_DIRECTORY).toString())
               .withFailureDirectory(importBasePath.resolve(DEFAULT_FAILURE_DIRECTORY).toString())
               .withProperties(getImporterProperties());
        return builder;
    }

    private static String defaultTimeZoneNotation() {
        int rawOffset =  TimeZone.getDefault().getRawOffset();
        long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset) - TimeUnit.HOURS.toMinutes(hours);
        return String.format("GMT%s%02d:%02d", rawOffset >= 0 ? "+" : "-", hours, minutes);
    }

    private static HasIdAndName defaultNumberFormat() {
        return  new HasIdAndName() {
                        @Override
                        public Object getId() {
                            return "FORMAT3";
                        }

                        @Override
                        public String getName() {
                            return "123456789.012";
                        }
                    };
    }


}
