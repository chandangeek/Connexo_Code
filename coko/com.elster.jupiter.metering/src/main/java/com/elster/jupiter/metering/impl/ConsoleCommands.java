package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationBuilder.LocationMemberBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.impl.config.DefaultReadingTypeTemplate;
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

@Component(name = "com.elster.jupiter.metering.console", service = ConsoleCommands.class, property = {
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
        "osgi.command.function=addUsagePointGeoCoordinates"

}, immediate = true)
public class ConsoleCommands {

    private volatile ServerMeteringService meteringService;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ServerMetrologyConfigurationService metrologyConfigurationService;

    public void printDdl() {
        try {
            for (Table<?> table : dataModel.getTables()) {
                for (Object s : table.getDdl()) {
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void meters() {
        meteringService.getMeterQuery().select(Condition.TRUE).stream()
                .map(meter -> meter.getId() + " " + meter.getMRID())
                .forEach(System.out::println);
    }

    public void usagePoints() {
        meteringService.getUsagePointQuery().select(Condition.TRUE).stream()
                .map(usagePoint -> usagePoint.getId() + " " + usagePoint.getMRID())
                .forEach(System.out::println);
    }

    public void meterActivations(long meterId) {
        Meter meter = meteringService.findMeter(meterId)
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
        String channels = meterActivation.getChannels().stream()
                .map(channel -> channel.getId() + " " + channel.getMainReadingType().getMRID())
                .collect(java.util.stream.Collectors.joining("\n\t"));
        return meterActivation.getRange().toString() + "\n\t" + channels;
    }

    public void createMeter(long amrSystemId, String amrid, String mrId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(amrSystemId)
                    .orElseThrow(() -> new IllegalArgumentException("amr System not found"));
            amrSystem.newMeter(amrid)
                    .setMRID(mrId)
                    .create();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void renameMeter(String mrId, String newName) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            meter.setName(newName);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void activateMeter(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant activationDate = Instant.ofEpochMilli(epochMilli);
            meter.activate(activationDate);
            meter.update();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void addUsagePointToCurrentMeterActivation() {
        System.out.println("Usage: addUsagePointToCurrentMeterActivation <mRID> <usage point mRID>");
    }

    public void addUsagePointToCurrentMeterActivation(String mrId, String usagePointmrId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            UsagePoint usagePoint = meteringService.findUsagePoint(usagePointmrId).get();
            MeterActivation meterActivation = meter.getCurrentMeterActivation().get();
            ((MeterActivationImpl) meterActivation).setUsagePoint(usagePoint);
            ((MeterActivationImpl) meterActivation).save();
            ((UsagePointImpl) usagePoint).adopt((MeterActivationImpl) meterActivation);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void endCurrentMeterActivation(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant endDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation().get().endAt(endDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void advanceStartDate(String mrId, long epochMilli) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            Meter meter = meteringService.findMeter(mrId).get();
            Instant newStartDate = Instant.ofEpochMilli(epochMilli);
            meter.getCurrentMeterActivation().get().advanceStartDate(newStartDate);
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void createUsagePoint() {
        System.out.println("Usage: createUsagePoint <mRID>");
    }

    public void createUsagePoint(String mrId, String timestamp) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            meteringService.getServiceCategory(ServiceKind.WATER)
                    .get()
                    .newUsagePoint(mrId, Instant.parse(timestamp))
                    .create();
            context.commit();
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void readingTypes() {
        meteringService.getAvailableReadingTypes().stream()
                .map(IdentifiedObject::getMRID)
                .forEach(System.out::println);
    }

    public void addEvents(String mrId, String dataFile) {
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
                        .map(line -> EndDeviceEventImpl.of(line[1], ZonedDateTime.parse(line[0], DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx"))
                                .toInstant()))
                        .collect(Collectors.toList());

                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                meterReading.addAllEndDeviceEvents(deviceEvents);
                meteringService.findMeter(mrId).get().store(meterReading);

                context.commit();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } finally {
            threadPrincipalService.clear();
        }
    }

    public void locationTemplate() {
        meteringService.getLocationTemplate().getTemplateElementsNames().stream()
                .forEach(System.out::println);
    }

    public void addDeviceLocation(String mRID, String... args) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDevice endDevice = meteringService.findEndDevice(mRID)
                    .orElseThrow(() -> new RuntimeException("No device with mRID " + mRID + "!"));
            endDevice.setLocation(getLocationBuilder(args).create());
            endDevice.update();
            context.commit();
        }
    }

    public void addDeviceLocation() {
        List<String> templateElements = meteringService.getLocationTemplate().getTemplateElementsNames();
        System.out.print("Example : addDeviceLocation Device_mRID ");
        templateElements.stream()
                .forEach(element -> System.out.print(element + " "));
        System.out.println();

    }

    public void addUsagePointLocation() {
        List<String> templateElements = meteringService.getLocationTemplate().getTemplateElementsNames();
        System.out.print("Example : addUsagePointLocation UsagePoint_mRID ");
        templateElements.stream()
                .forEach(element -> System.out.print(element + " "));
        System.out.println();

    }

    public void addUsagePointLocation(String mRID, String... args) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePoint(mRID)
                    .orElseThrow(() -> new RuntimeException("No usage point with mRID " + mRID + "!"));
            usagePoint.setLocation(getLocationBuilder(args).create().getId());
            usagePoint.update();
            context.commit();
        }
    }

    public void addDeviceGeoCoordinates() {
        System.out.print("Example : addDeviceGeoCoordinates Device_mRID latitude longitude elevation");
        System.out.println();
    }

    public void addDeviceGeoCoordinates(String mRID, String latitude, String longitude, String elevation) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            EndDevice endDevice = meteringService.findEndDevice(mRID)
                    .orElseThrow(() -> new RuntimeException("No device with mRID " + mRID + "!"));
            GeoCoordinates geoCoordinates = meteringService.createGeoCoordinates(latitude + ":" + longitude + ":" + elevation);
            endDevice.setGeoCoordintes(geoCoordinates);
            endDevice.update();
            context.commit();
        }
    }

    public void addUsagePointGeoCoordinates() {
        System.out.print("Example : addUsagePointGeoCoordinates UsagePoint_mRID latitude longitude elevation ");
        System.out.println();
    }

    public void addUsagePointGeoCoordinates(String mRID, String latitude, String longitude, String elevation) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            UsagePoint usagePoint = meteringService.findUsagePoint(mRID)
                    .orElseThrow(() -> new RuntimeException("No usage point with mRID " + mRID + "!"));
            GeoCoordinates geoCoordinates = meteringService.createGeoCoordinates(latitude + ":" + longitude + ":" + elevation);
            usagePoint.setGeoCoordinates(geoCoordinates);
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
            throw new RuntimeException("Location provided does not meet template length !");
        } else {
            IntStream.range(0, args.length)
                    .forEach(i -> location.put(templateElements.get(i), args[i]));
            LocationBuilder builder = meteringService.newLocationBuilder();
            Optional<LocationMemberBuilder> memberBuilder = builder.getMember(location.get("locale"));
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
            MetrologyConfiguration config = metrologyConfigurationService.newMetrologyConfiguration(name, serviceCategory)
                    .create();
            System.out.println(config.getId() + ": " + config.getName());
            context.commit();
        }
    }

    public void deleteMetrologyConfig(long id) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            metrologyConfigurationService.findMetrologyConfiguration(id)
                    .orElseThrow(() -> new NoSuchElementException("No such metrology configuration"))
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
                    long id = upMetrologyConfiguration.newReadingTypeRequirement(name).withMeterRole(meterRole).withReadingType(readingType).getId();
                    System.out.println("Requirment created with id: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown default meter role: " + meterRoleName + ". Use one of: " + Stream.of(DefaultMeterRole.values()).map(DefaultMeterRole::name).collect(Collectors.joining(", ")));
                    throw e;
                }
            } else {
                metrologyConfiguration.newReadingTypeRequirement(name).withReadingType(readingType);
            }
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
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
                    long id = upMetrologyConfiguration.newReadingTypeRequirement(name).withMeterRole(meterRole).withReadingTypeTemplate(template).getId();
                    System.out.println("Requirment created with id: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown default meter role: " + meterRoleName + ". Use one of: " + Stream.of(DefaultMeterRole.values()).map(DefaultMeterRole::name).collect(Collectors.joining(", ")));
                    throw e;
                }
            } else {
                metrologyConfiguration.newReadingTypeRequirement(name).withReadingTypeTemplate(template);
            }
            context.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
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
                .map(del -> del.getId() + " " + del.getName() + " " + del.getReadingType().getMRID() + " root node: " + this.getExpressionNode(del).getId())
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

    public void doAddDeliverable(long metrologyConfigId, String name, String readingTypeString, String formulaString, Formula.Mode mode) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                    .orElseThrow(() -> new IllegalArgumentException("No such metrology configuration"));
            ReadingType readingType = meteringService.getReadingType(readingTypeString)
                    .orElseThrow(() -> new IllegalArgumentException("No such reading type"));

            ServerExpressionNode node = new ExpressionNodeParser(meteringService.getThesaurus(), metrologyConfigurationService, metrologyConfiguration, mode).parse(formulaString);

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
            ExpressionNode node = new ExpressionNodeParser(meteringService.getThesaurus(), metrologyConfigurationService, deliverable.getMetrologyConfiguration(), Formula.Mode.AUTO).parse(formulaString);

            deliverable.setName(name);
            deliverable.setReadingType(readingType);
            deliverable.getFormula().updateExpression(node);
            deliverable.update();

            context.commit();
        }
    }

    public void updateDeliverableReadingType() {
        System.out.println("Usage: updateDeliverableReadingType  <deliverable id> <reading type>");
    }

    public void updateDeliverableReadingType(long deliverableId, String readingTypeString) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            try {
                ReadingTypeDeliverable deliverable = metrologyConfigurationService.findReadingTypeDeliverable(deliverableId)
                        .orElseThrow(() -> new IllegalArgumentException("No such deliverable"));
                ReadingType readingType = meteringService.getReadingType(readingTypeString)
                        .orElseThrow(() -> new IllegalArgumentException("No such reading type"));

                deliverable.setReadingType(readingType);
                deliverable.update();
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
            ExpressionNode node = new ExpressionNodeParser(meteringService.getThesaurus(), metrologyConfigurationService, deliverable.getMetrologyConfiguration(), Formula.Mode.AUTO).parse(formulaString);

            deliverable.getFormula().updateExpression(node);
            deliverable.update();
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
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId).get().addMetrologyContract(purpose);
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
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId).get().addMetrologyContract(purpose);
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
            MetrologyContract contract = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId).get().addMetrologyContract(purpose);
            contract.removeDeliverable(deliverable);
            context.commit();
        }
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
        this.dataModel = meteringService.getDataModel();
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setMetrologyConfigurationService(ServerMetrologyConfigurationService metrologyConfigurationService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private String explained(ReadingType readingType) {
        StringBuilder builder = new StringBuilder();
        builder.append("Macro period             : ")
                .append(readingType.getMacroPeriod().getDescription())
                .append('\n');
        builder.append("Aggregate                : ").append(readingType.getAggregate().getDescription()).append('\n');
        builder.append("Measuring period         : ")
                .append(readingType.getMeasuringPeriod().getDescription())
                .append('\n');
        builder.append("Accumulation             : ")
                .append(readingType.getAccumulation().getDescription())
                .append('\n');
        builder.append("Flow direction           : ")
                .append(readingType.getFlowDirection().getDescription())
                .append('\n');
        builder.append("Commodity                : ").append(readingType.getCommodity().getDescription()).append('\n');
        builder.append("Macro period             : ")
                .append(readingType.getMacroPeriod().getDescription())
                .append('\n');
        builder.append("Measurement kind         : ")
                .append(readingType.getMeasurementKind().getDescription())
                .append('\n');
        builder.append("IH numerator             : ")
                .append(readingType.getInterharmonic().getNumerator())
                .append('\n');
        builder.append("IH denominator           : ")
                .append(readingType.getInterharmonic().getDenominator())
                .append('\n');
        builder.append("Argument numerator       : ").append(readingType.getArgument().getNumerator()).append('\n');
        builder.append("Argument denominator     : ").append(readingType.getArgument().getDenominator()).append('\n');
        builder.append("Time of use              : ").append(readingType.getTou()).append('\n');
        builder.append("CPP                      : ").append(readingType.getCpp()).append('\n');
        builder.append("Consumption tier         : ").append(readingType.getConsumptionTier()).append('\n');
        builder.append("Phases                   : ").append(readingType.getPhases().getDescription()).append('\n');
        builder.append("Multiplier               : ").append(readingType.getMultiplier()).append('\n');
        builder.append("Unit                     : ").append(readingType.getUnit()).append('\n');
        builder.append("Currency                 : ").append(readingType.getCurrency().toString()).append('\n');
        return builder.toString();
    }

}