package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationBuilder.LocationMemberBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.DefaultReadingTypeTemplate;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.ExpressionNodeParser;
import com.elster.jupiter.metering.impl.config.ReadingTypeDeliverableBuilderImpl;
import com.elster.jupiter.metering.impl.config.ServerExpressionNode;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.metering.console", service = MeteringConsoleCommands.class, property = {
        "osgi.command.scope=metering",
        "osgi.command.function=printDdl",
        "osgi.command.function=meters",
        "osgi.command.function=usagePoints",
        "osgi.command.function=readingTypes",
        "osgi.command.function=createMeter",
        "osgi.command.function=createUsagePoint",
        "osgi.command.function=channelConfig",
        "osgi.command.function=meterActivations",
        "osgi.command.function=renameMeter",
        "osgi.command.function=activateMeter",
        "osgi.command.function=addUsagePointToCurrentMeterActivation",
        "osgi.command.function=endCurrentMeterActivation",
        "osgi.command.function=advanceStartDate",
        "osgi.command.function=explain",
        "osgi.command.function=locationTemplate",
        "osgi.command.function=addEvents",
        "osgi.command.function=formulas",
        "osgi.command.function=addMetrologyConfig",
        "osgi.command.function=deleteMetrologyConfig",
        "osgi.command.function=addRequirement",
        "osgi.command.function=addRequirementWithTemplateReadingType",
        "osgi.command.function=deliverables",
        "osgi.command.function=addDeliverable",
        "osgi.command.function=addDeliverableExpert",
        "osgi.command.function=updateDeliverable",
        "osgi.command.function=updateDeliverableReadingType",
        "osgi.command.function=updateDeliverableFormula",
        "osgi.command.function=deleteDeliverable",
        "osgi.command.function=getDeliverablesOnContract",
        "osgi.command.function=addDeliverableToContract",
        "osgi.command.function=removeDeliverableFromContract",
        "osgi.command.function=metrologyConfigs",
        "osgi.command.function=addDeviceLocation",
        "osgi.command.function=addDeviceGeoCoordinates",
        "osgi.command.function=addUsagePointLocation",
        "osgi.command.function=addUsagePointGeoCoordinates",
        "osgi.command.function=activateMetrologyConfig",
        "osgi.command.function=addCustomPropertySet"
}, immediate = true)
@SuppressWarnings("unused")
public class MeteringConsoleCommands {

    private volatile ServerMeteringService meteringService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServerMetrologyConfigurationService metrologyConfigurationService;

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMetrologyConfigurationService(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
        this.dataModel = meteringService.getDataModel();
    }

    public void printDdl() {
        try {
            dataModel.getTables().stream()
                    .map(Table::getDdl)
                    .flatMap(List::stream)
                    .forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void meters() {
        meteringService.getMeterQuery().select(Condition.TRUE).stream()
                .map(meter -> meter.getId() + ' ' + meter.getName() + ' ' + meter.getMRID())
                .forEach(System.out::println);
    }

    public void usagePoints() {
        meteringService.getUsagePointQuery().select(Condition.TRUE).stream()
                .map(usagePoint -> usagePoint.getId() + ' ' + usagePoint.getName() + ' ' + usagePoint.getMRID())
                .forEach(System.out::println);
    }

    public void meterActivations(long meterId) {
        Meter meter = meteringService.findMeterById(meterId)
                .orElseThrow(() -> new IllegalArgumentException("Meter not found."));
        System.out.println(meter.getMeterActivations().stream()
                .map(this::toString)
                .collect(java.util.stream.Collectors.joining("\n")));
    }

    public void explain(String readingType) {
        System.out.println(explained(meteringService.getReadingType(readingType)
                .orElseThrow(() -> new IllegalArgumentException("ReadingType does not exist."))));
    }

    private String toString(MeterActivation meterActivation) {
        String channels = meterActivation.getChannelsContainer().getChannels().stream()
                .map(channel -> channel.getId() + " " + channel.getMainReadingType().getMRID())
                .collect(java.util.stream.Collectors.joining("\n\t"));
        String activationDetails = meterActivation.getUsagePoint()
                .map(usagePoint ->
                        "activation on usage point '" + usagePoint.getName()
                                + "' in role '" + meterActivation.getMeterRole().get().getDisplayName() + "'"
                ).orElse("");
        return meterActivation.getRange().toString() + " " + activationDetails + "\n\t" + channels;
    }

    public void createMeter() {
        System.out.println("Usage: createMeter <amrSystemId: usually 2> <amrId: EA_MS Meter ID> <name>");
    }

    public void createMeter(long amrSystemId, String amrId, String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId)
                    .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            Meter meter = amrSystem.newMeter(amrId, name)
                    .create();
            context.commit();
            System.out.println("Meter " + amrId
                    + " created with ID: " + meter.getId()
                    + ", MRID: " + meter.getMRID());
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void renameMeter(String name, String newName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeterByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Meter " + name + " does not exist"));
            meter.setName(newName);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void activateMeter() {
        System.out.println("Usage activateMeter <name> <epoch millis> [<meter role> <usage point name>]");
        System.out.println("       where meter role is one of: "
                + Stream.of(DefaultMeterRole.values()).map(DefaultMeterRole::name).collect(Collectors.joining(", ")));
    }

    public void activateMeter(String name, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeterByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("Meter " + name + " does not exist"));
            Instant activationDate = Instant.ofEpochMilli(epochMilli);
            meter.activate(activationDate);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void activateMeter(String meterName, long epochMilli, String defaultMeterRoleName, String usagePointName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            DefaultMeterRole defaultMeterRole = DefaultMeterRole.valueOf(defaultMeterRoleName);
            MeterRole meterRole = this.metrologyConfigurationService.findDefaultMeterRole(defaultMeterRole);
            Meter meter = meteringService.findMeterByName(meterName)
                    .orElseThrow(() -> new IllegalArgumentException("Meter " + meterName + " does not exist"));
            UsagePoint usagePoint = this.meteringService.findUsagePointByName(usagePointName)
                    .orElseThrow(() -> new IllegalArgumentException("Usage point " + usagePointName + " does not exist"));
            Instant activationDate = Instant.ofEpochMilli(epochMilli);
            usagePoint.linkMeters().activate(activationDate, meter, meterRole).complete();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void addUsagePointToCurrentMeterActivation() {
        System.out.println("Usage: addUsagePointToCurrentMeterActivation <meter name> <usage point name>");
    }

    public void addUsagePointToCurrentMeterActivation(String meterName, String usagePointName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeterByName(meterName)
                    .orElseThrow(() -> new IllegalArgumentException("No meter with name " + meterName));
            UsagePointImpl usagePoint = meteringService.findUsagePointByName(usagePointName)
                    .map(UsagePointImpl.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException("No usage point with name " + usagePointName));
            MeterActivationImpl meterActivation = meter.getCurrentMeterActivation()
                    .map(MeterActivationImpl.class::cast)
                    .orElseThrow(() -> new IllegalArgumentException("No current meter activation on meter " + meterName));
            meterActivation.setUsagePoint(usagePoint);
            meterActivation.save();
            usagePoint.adopt(meterActivation);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void endCurrentMeterActivation(String name, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeterByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No meter with name " + name));
            Instant endDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation()
                    .orElseThrow(() -> new IllegalArgumentException("No current meter activation on meter " + name))
                    .endAt(endDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void advanceStartDate(String name, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeterByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No meter with name " + name));
            Instant newStartDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation()
                    .orElseThrow(() -> new IllegalArgumentException("No current meter activation on meter " + name))
                    .advanceStartDate(newStartDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void createUsagePoint() {
        System.out.println("Usage: createUsagePoint <name> <installation datetime format 2011-12-03T10:15:30Z>");
    }

    public void createUsagePoint(String name, String timestamp) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.getServiceCategory(ServiceKind.WATER)
                    .orElseThrow(() -> new NoSuchElementException("No Water service category found"))
                    .newUsagePoint(name, Instant.parse(timestamp + "T00:00:00Z"))
                    .create();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void readingTypes() {
        meteringService.getAvailableReadingTypes().stream()
                .map(ReadingType::getMRID)
                .forEach(System.out::println);
    }

    public void addEvents(String name, String dataFile) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            File eventData = new File(dataFile);
            try (Scanner scanner = new Scanner(eventData)) {
                List<String> lines = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }

                List<EndDeviceEventImpl> deviceEvents = lines.stream()
                        .map(line -> line.split(";"))
                        .map(line -> EndDeviceEventImpl.of(line[1],
                                ZonedDateTime.parse(line[0], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")).toInstant()))
                        .collect(Collectors.toList());

                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                meterReading.addAllEndDeviceEvents(deviceEvents);
                meteringService.findMeterByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("No meter with name " + name))
                        .store(QualityCodeSystem.MDC, meterReading);

                context.commit();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void locationTemplate() {
        meteringService.getLocationTemplate().getTemplateElementsNames().forEach(System.out::println);
    }

    public void addDeviceLocation(String name, String... args) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDevice endDevice = meteringService.findEndDeviceByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No end device with name " + name + "!"));
            endDevice.setLocation(getLocationBuilder(args).create());
            endDevice.update();
            context.commit();
        }
    }

    public void addDeviceLocation() {
        System.out.println("Example : addDeviceLocation <end device name>");
        locationTemplate();
    }

    public void addUsagePointLocation() {
        System.out.println("Example : addUsagePointLocation <usagePointName>");
        locationTemplate();
    }

    public void addUsagePointLocation(String name, String... args) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePointByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No usage point with name " + name + "!"));
            usagePoint.setLocation(getLocationBuilder(args).create().getId());
            usagePoint.update();
            context.commit();
        }
    }

    public void addDeviceGeoCoordinates() {
        System.out.println("Example : addDeviceGeoCoordinates <end device name> <latitude> <longitude> <elevation>");
    }

    public void addDeviceGeoCoordinates(String name, String latitude, String longitude, String elevation) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDevice endDevice = meteringService.findEndDeviceByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No device with name " + name + "!"));

            SpatialCoordinates spatialCoordinates = new SpatialCoordinatesFactory().fromStringValue(latitude + ":" + longitude + ":" + elevation);
            endDevice.setSpatialCoordinates(spatialCoordinates);
            endDevice.update();
            context.commit();
        }
    }

    public void addUsagePointGeoCoordinates() {
        System.out.println("Example : addUsagePointGeoCoordinates <usagePointName> <latitude> <longitude> <elevation>");
    }

    public void addUsagePointGeoCoordinates(String name, String latitude, String longitude, String elevation) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePointByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("No usage point with name " + name + "!"));
            SpatialCoordinates spatialCoordinates = new SpatialCoordinatesFactory().fromStringValue(latitude + ":" + longitude + ":" + elevation);
            usagePoint.setSpatialCoordinates(spatialCoordinates);
            usagePoint.update();
            context.commit();
        }
    }

    private LocationMemberBuilder setLocationAttributes(LocationMemberBuilder builder, Map<String, String> location) {
        builder.setCountryCode(location.get("countryCode"))
                .setCountryName(location.get("countryName"))
                .setAdministrativeArea(location.get("administrativeArea"))
                .setLocality(location.get("locality"))
                .setSubLocality(location.get("subLocality"))
                .setStreetType(location.get("streetType"))
                .setStreetName(location.get("streetName"))
                .setStreetNumber(location.get("streetNumber"))
                .setEstablishmentType(location.get("establishmentType"))
                .setEstablishmentName(location.get("establishmentName"))
                .setEstablishmentNumber(location.get("establishmentNumber"))
                .setAddressDetail(location.get("addressDetail"))
                .setZipCode(location.get("zipCode"))
                .isDaultLocation(true)
                .setLocale(location.get("locale"));
        return builder;
    }

    private LocationBuilder getLocationBuilder(String... args) {
        List<String> templateElements = meteringService.getLocationTemplate().getTemplateElementsNames();
        Map<String, String> location = new HashMap<>();
        if (templateElements.size() != args.length) {
            throw new IllegalArgumentException("Location provided does not meet template length!");
        } else {
            IntStream.range(0, args.length)
                    .forEach(i -> location.put(templateElements.get(i), args[i]));
            LocationBuilder builder = new LocationBuilderImpl(dataModel);
            Optional<LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(location.get("locale"));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), location);
            } else {
                setLocationAttributes(builder.member(), location).add();
            }
            return builder;
        }
    }

    public void formulas() {
        metrologyConfigurationService.findFormulas().stream()
                .map(Formula::toString)
                .forEach(System.out::println);
    }

    public void addMetrologyConfig(String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(() -> new NoSuchElementException("Service category not found: " + ServiceKind.ELECTRICITY));
            UsagePointMetrologyConfiguration config = metrologyConfigurationService.newUsagePointMetrologyConfiguration(name, serviceCategory)
                    .create();
            MeterRole meterRole = metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Default meter role not found"));
            config.addMeterRole(meterRole);
            System.out.println(config.getId() + ": " + config.getName());
            context.commit();
        }
    }

    public void deleteMetrologyConfig(long id) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            metrologyConfigurationService.findMetrologyConfiguration(id)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                    .delete();
            context.commit();
        }
    }

    public void metrologyConfigs() {
        for (MetrologyConfiguration config : metrologyConfigurationService.findAllMetrologyConfigurations()) {
            System.out.println(config.getId() + ": " + config.getName());
        }
    }

    public void addRequirement() {
        System.out.println("Usage: addRequirement <name> <reading type> [<meter role>] <metrology configuration id>");
    }

    public void addRequirement(String name, String readingTypeString, long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            ReadingType readingType = meteringService.getReadingType(readingTypeString)
                    .orElseThrow(() -> new IllegalArgumentException("No such reading type"));
            if (metrologyConfiguration instanceof UsagePointMetrologyConfiguration) {
                throw new IllegalArgumentException("MetrologyConfiguration requires that you specify a meter role");
            } else {
                long id = metrologyConfiguration.newReadingTypeRequirement(name).withReadingType(readingType).getId();
                System.out.println("Requirement created with id: " + id);
            }
            context.commit();
        }
    }

    public void addRequirement(String name, String readingTypeString, String meterRoleName, long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            ReadingType readingType = meteringService.getReadingType(readingTypeString)
                    .orElseThrow(() -> new IllegalArgumentException("No such reading type"));
            if (metrologyConfiguration instanceof UsagePointMetrologyConfiguration) {
                try {
                    DefaultMeterRole defaultMeterRole = DefaultMeterRole.valueOf(meterRoleName);
                    MeterRole meterRole = this.metrologyConfigurationService.findDefaultMeterRole(defaultMeterRole);
                    UsagePointMetrologyConfiguration upMetrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfiguration;
                    long id = upMetrologyConfiguration.newReadingTypeRequirement(name, meterRole).withReadingType(readingType).getId();
                    System.out.println("Requirment created with id: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown default meter role: " + meterRoleName + ". Use one of: " + Stream.of(DefaultMeterRole
                            .values())
                            .map(DefaultMeterRole::name)
                            .collect(Collectors.joining(", ")));
                    throw e;
                }
            } else {
                metrologyConfiguration.newReadingTypeRequirement(name).withReadingType(readingType);
            }
            context.commit();
        }
    }

    public void addRequirementWithTemplateReadingType() {
        System.out.println("Usage: addRequirementWithTemplateReadingType <name> <reading type template> <meter role> <metrology configuration id>");
    }

    public void addRequirementWithTemplateReadingType(String name, String defaultTemplate, String meterRoleName, long metrologyConfigId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            ReadingTypeTemplate template = metrologyConfigurationService.createReadingTypeTemplate(DefaultReadingTypeTemplate.valueOf(defaultTemplate)).done();

            if (metrologyConfiguration instanceof UsagePointMetrologyConfiguration) {
                try {
                    DefaultMeterRole defaultMeterRole = DefaultMeterRole.valueOf(meterRoleName);
                    MeterRole meterRole = this.metrologyConfigurationService.findDefaultMeterRole(defaultMeterRole);
                    UsagePointMetrologyConfiguration upMetrologyConfiguration = (UsagePointMetrologyConfiguration) metrologyConfiguration;
                    long id = upMetrologyConfiguration.newReadingTypeRequirement(name, meterRole).withReadingTypeTemplate(template).getId();
                    System.out.println("Requirment created with id: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown default meter role: " + meterRoleName + ". Use one of: " + Stream.of(DefaultMeterRole
                            .values())
                            .map(DefaultMeterRole::name)
                            .collect(Collectors.joining(", ")));
                    throw e;
                }
            } else {
                metrologyConfiguration.newReadingTypeRequirement(name).withReadingTypeTemplate(template);
            }
            context.commit();
        }
    }

    public void deliverables() {
        System.out.println("Usage: deliverables <metrology configuration id>");
    }

    public void deliverables(long id) {
        printDeliverables(metrologyConfigurationService.findMetrologyConfiguration(id)
                .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                .getDeliverables());
    }

    private void printDeliverables(List<ReadingTypeDeliverable> deliverables) {
        deliverables.stream()
                .map(del -> del.getId() + " " + del.getName() + " " + del.getReadingType().getMRID()
                        + " root node: " + this.getExpressionNode(del).getId())
                .forEach(System.out::println);
    }

    private ServerExpressionNode getExpressionNode(ReadingTypeDeliverable deliverable) {
        return this.getExpressionNode(deliverable.getFormula());
    }

    private ServerExpressionNode getExpressionNode(Formula formula) {
        return (ServerExpressionNode) formula.getExpressionNode();
    }

    public void addDeliverable() {
        System.out.println("Usage: addDeliverable  <metrology configuration id> <name> <reading type> <formula string>");
    }

    public void addDeliverable(long metrologyConfigId, String name, String readingTypeString, String formulaString) {
        doAddDeliverable(metrologyConfigId, name, readingTypeString, formulaString, Formula.Mode.AUTO);
    }

    public void addDeliverableExpert(long metrologyConfigId, String name, String readingTypeString, String formulaString) {
        doAddDeliverable(metrologyConfigId, name, readingTypeString, formulaString, Formula.Mode.EXPERT);
    }

    private void doAddDeliverable(long metrologyConfigId, String name, String readingTypeString, String formulaString, Formula.Mode mode) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            ReadingType readingType = meteringService.getReadingType(readingTypeString)
                    .orElseThrow(() -> new IllegalArgumentException("No such reading type"));

            ServerExpressionNode node = new ExpressionNodeParser(meteringService.getThesaurus(),
                    metrologyConfigurationService, customPropertySetService, metrologyConfiguration, mode).parse(formulaString);

            long id = ((ReadingTypeDeliverableBuilderImpl) metrologyConfiguration.newReadingTypeDeliverable(name, readingType, mode)).build(node).getId();
            System.out.println("Deliverable created: " + id);
            context.commit();
        }
    }

    public void updateDeliverable() {
        System.out.println("Usage: updateDeliverable  <deliverable id> <name> <reading type> <formula string>");
    }

    public void updateDeliverable(long deliverableId, String name, String readingTypeString, String formulaString) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                    .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
            ReadingType readingType = meteringService.getReadingType(readingTypeString)
                    .orElseThrow(() -> new IllegalArgumentException("No such reading type"));
            deliverable
                    .startUpdate()
                    .setName(name)
                    .setReadingType(readingType)
                    .setFormula(formulaString)
                    .complete();
            context.commit();
        }
    }

    public void updateDeliverableReadingType() {
        System.out.println("Usage: updateDeliverableReadingType <deliverable id> <reading type>");
    }

    public void updateDeliverableReadingType(long deliverableId, String readingTypeString) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            try {
                ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                        .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
                ReadingType readingType = meteringService.getReadingType(readingTypeString)
                        .orElseThrow(() -> new IllegalArgumentException("No such reading type"));
                deliverable
                        .startUpdate()
                        .setReadingType(readingType)
                        .complete();
                context.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateDeliverableFormula() {
        System.out.println("Usage: updateDeliverableFormula  <deliverable id> <formula string>");
    }

    public void updateDeliverableFormula(long deliverableId, String formulaString) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                    .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
            deliverable
                    .startUpdate()
                    .setFormula(formulaString)
                    .complete();
            context.commit();
        }
    }

    public void deleteDeliverable() {
        System.out.println("Usage: deleteDeliverable <metrology configuration id> <deliverable id>");
    }

    public void deleteDeliverable(long metrologyConfigId, long deliverableId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                    .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            metrologyConfiguration.removeReadingTypeDeliverable(deliverable);
            context.commit();
        }
    }

    public void getDeliverablesOnContract() {
        System.out.println("Usage: getDeliverablesOnContract <metrology configuration id> <default purpose>");
    }

    public void getDeliverablesOnContract(long metrologyConfigId, String defaultPurpose) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.valueOf(defaultPurpose))
                    .orElseThrow(() -> new NoSuchElementException("Default purposes not installed"));
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                    .addMetrologyContract(purpose);
            printDeliverables(contract.getDeliverables());
        }
    }

    public void addDeliverableToContract() {
        System.out.println("Usage: addDeliverableToContract <metrology configuration id> <deliverable id> <default purpose>");
    }

    public void addDeliverableToContract(long metrologyConfigId, long deliverableId, String defaultPurpose) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.valueOf(defaultPurpose))
                    .orElseThrow(() -> new NoSuchElementException("Default purposes not installed"));
            ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                    .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                    .addMetrologyContract(purpose);
            contract.addDeliverable(deliverable);
            context.commit();
        }
    }

    public void removeDeliverableFromContract() {
        System.out.println("Usage: removeDeliverableFromContract <metrology configuration id> <deliverable id> <default purpose>");
    }

    public void removeDeliverableFromContract(long metrologyConfigId, long deliverableId, String defaultPurpose) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyPurpose purpose = metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.valueOf(defaultPurpose))
                    .orElseThrow(() -> new NoSuchElementException("Default purposes not installed"));
            ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                    .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                    .addMetrologyContract(purpose);
            contract.removeDeliverable(deliverable);
            context.commit();
        }
    }

    private String explained(ReadingType readingType) {
        return "Macro period             : " +
                readingType.getMacroPeriod().getDescription() +
                '\n' +
                "Aggregate                : " + readingType.getAggregate().getDescription() + '\n' +
                "Measuring period         : " +
                readingType.getMeasuringPeriod().getDescription() +
                '\n' +
                "Accumulation             : " +
                readingType.getAccumulation().getDescription() +
                '\n' +
                "Flow direction           : " +
                readingType.getFlowDirection().getDescription() +
                '\n' +
                "Commodity                : " + readingType.getCommodity().getDescription() + '\n' +
                "Macro period             : " +
                readingType.getMacroPeriod().getDescription() +
                '\n' +
                "Measurement kind         : " +
                readingType.getMeasurementKind().getDescription() +
                '\n' +
                "IH numerator             : " +
                readingType.getInterharmonic().getNumerator() +
                '\n' +
                "IH denominator           : " +
                readingType.getInterharmonic().getDenominator() +
                '\n' +
                "Argument numerator       : " + readingType.getArgument().getNumerator() + '\n' +
                "Argument denominator     : " + readingType.getArgument().getDenominator() + '\n' +
                "Time of use              : " + readingType.getTou() + '\n' +
                "CPP                      : " + readingType.getCpp() + '\n' +
                "Consumption tier         : " + readingType.getConsumptionTier() + '\n' +
                "Phases                   : " + readingType.getPhases().getDescription() + '\n' +
                "Multiplier               : " + readingType.getMultiplier() + '\n' +
                "Unit                     : " + readingType.getUnit() + '\n' +
                "Currency                 : " + readingType.getCurrency().toString() + '\n';
    }

    public void activateMetrologyConfig(String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            metrologyConfigurationService.findMetrologyConfiguration(name)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"))
                    .activate();
            context.commit();
        }
    }

    public void addCustomPropertySet() {
        System.out.println("Usage: addCustomPropertySet <metrology configuration id> <custom property set id>");
    }

    @SuppressWarnings("unchecked")
    public void addCustomPropertySet(long metrologyConfigurationId, String customPropertySetId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            RegisteredCustomPropertySet customPropertySet = this.customPropertySetService
                    .findActiveCustomPropertySet(customPropertySetId)
                    .orElseThrow(() -> new IllegalArgumentException("Unable to find custom property set with id " + customPropertySetId));
            MetrologyConfiguration metrologyConfiguration =
                    this.metrologyConfigurationService
                            .findMetrologyConfiguration(metrologyConfigurationId)
                            .orElseThrow(() -> new IllegalArgumentException("Unable to find metrology configuration with id " + metrologyConfigurationId));
            metrologyConfiguration.addCustomPropertySet(customPropertySet);
            context.commit();
        }
    }

}
