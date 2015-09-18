package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.ImportScheduleBuilder;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.time.PeriodicalScheduleExpression;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Copyrights EnergyICT
 * Date: 15/09/2015
 * Time: 11:04
 */
public enum FileImporterTpl implements Template<ImportSchedule, ImportScheduleBuilder> {

    DEVICE_INSTALLATION_IMPORTER("Installation", "DeviceInstallationImporterFactory") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_READINGS_IMPORTER("Import readings", "DeviceReadingsImporterFactory") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    CONNECTION_ATTRIBUTES_IMPORTER("Import connection attributes", "ConnectionAttributesImportFactory"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    SECURITY_ATTRIBUTES_IMPORTER("Import security attributes", "SecurityAttributesImportFactory"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.numberFormat", defaultNumberFormat());
            return properties;
        }
    },
    DEVICE_ACTIVATION_DEACTIVATION_IMPORTER("Activation Deactivation","DeviceActivationDeactivationImportFactory"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_COMMISSIONING_IMPORTER("Commissioning", "DeviceCommissioningImportFactory"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_DECOMMISSIONG_IMPORTER("Decommissioning", "DeviceDecommissioningImportFactory"){
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone",defaultTimeZoneNotation());
            return properties;
        }
    },
    DEVICE_REMOVE_IMPORTER("Remove", "DeviceRemoveImportFactory"),
    DEVICE_SHIPMENT_IMPORTER("Shipment", "DeviceShipmentImporterFactory") {
        protected Map<String, Object> getImporterProperties() {
            Map<String, Object> properties = super.getImporterProperties();
            properties.put("DeviceDataFileImporterFactory.dateFormat", DATE_AND_TIME_PATTERN);
            properties.put("DeviceDataFileImporterFactory.timeZone", defaultTimeZoneNotation());
            return properties;
        }
    };

    public static final String DATE_AND_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private final static String DEFAULT_IMPORT_DIRECTORY = "D:\\EnergyICT\\Connexo\\PRD\\Data-Import";
    private final static String DEFAULT_PROCESSING_DIRECTORY = "D:\\EnergyICT\\Connexo\\PRD\\Data-Import\\Progress";
    private final static String DEFAULT_SUCCESS_DIRECTORY = "D:\\EnergyICT\\Connexo\\PRD\\Data-Import\\Success";
    private final static String DEFAULT_FAILURE_DIRECTORY = "D:\\EnergyICT\\Connexo\\PRD\\Data-Import\\Failure";
    private final static String DEFAULT_PATH_MATCHER = "*.csv";
    private final static PeriodicalScheduleExpression DEFAULT_SCHEDULED_EXPRESSION = PeriodicalScheduleExpression.every(1).minutes().at(0).build();

    private String importerName;
    private String factoryName;

    FileImporterTpl(String importerName, String factoryName) {
        this.importerName = importerName;
        this.factoryName = factoryName;
    }

    protected Map<String, Object> getImporterProperties() {
        HashMap<String, Object> properties = new HashMap<>();
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
                .withImportDirectory(DEFAULT_IMPORT_DIRECTORY)
                .withInProcessDirectory(DEFAULT_PROCESSING_DIRECTORY)
                .withSuccessDirectory(DEFAULT_SUCCESS_DIRECTORY)
                .withFailureDirectory(DEFAULT_FAILURE_DIRECTORY)
                .withProperties(getImporterProperties());
        return builder;
    }

    private static String defaultTimeZoneNotation() {
        int rawOffset =  TimeZone.getDefault().getRawOffset();
        long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset) - TimeUnit.HOURS.toMinutes(hours);
        return String.format("GMT%s%02d:%02d", rawOffset >= 0 ? "+" : "-", hours, minutes);
    }

    private static HasIdAndName defaultNumberFormat(){
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
