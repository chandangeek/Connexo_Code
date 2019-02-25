/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.channelRegisterSpecifications;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class AuditTrailDeviceSpecificationDecoder extends AbstractDeviceAuditDecoder {

    AuditTrailDeviceSpecificationDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Optional<ReadingTypeObisCodeUsage> readingTypeObisCodeUsage = getReadingTypeObisCodeUsage();
        String fullAliasName =  readingTypeObisCodeUsage
                .map(rtocu -> rtocu.getReadingType().getFullAliasName())
                .orElseGet(() -> getMeterConfiguration()
                        .map(mc -> mc.getMeasured().getFullAliasName())
                        .orElseGet(() -> "")
                );

        device.ifPresent(dev -> {
            dev.getChannels().stream()
                    .filter(channel -> channel.getReadingType().getFullAliasName().equals(fullAliasName))
                    .findFirst()
                    .map(channel -> {
                        builder.put("sourceType", "CHANNEL");
                        builder.put("sourceTypeName", getDisplayName(PropertyTranslationKeys.CHANNEL));
                        builder.put("sourceId", channel.getId());
                        return builder;
                    });
        });
        device.ifPresent(dev -> {
            dev.getRegisters().stream()
                    .filter(regiter -> regiter.getReadingType().getFullAliasName().equals(fullAliasName))
                    .findFirst()
                    .map(regiter -> {
                        builder.put("sourceType", "REGISTER");
                        builder.put("sourceTypeName", getDisplayName(PropertyTranslationKeys.REGISTER));
                        builder.put("sourceId", regiter.getRegisterSpecId());
                        return builder;
                    });
        });
        builder.put("sourceName", fullAliasName);
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            getAuditLogChangeForDevice().ifPresent(auditLogChanges::add);
            auditLogChanges.addAll(getAuditLogChangeForEndDevice());

            return auditLogChanges
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }


    private Optional<AuditLogChange> getAuditLogChangeForDevice() {
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ReadingTypeObisCodeUsage> dataMapper = dataModel.mapper(ReadingTypeObisCodeUsage.class);

        List<ReadingTypeObisCodeUsage> actualEntries = getActualEntries(dataMapper, getActualClauses());
        List<ReadingTypeObisCodeUsage> historyByModTimeEntries = getHistoryEntries(dataMapper, getHistoryByModTimeClauses(device.get().getId()));
        List<ReadingTypeObisCodeUsage> historyByJournalTimeEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(device.get().getId()));

        Optional<ReadingTypeObisCodeUsage> to = actualEntries.stream()
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> historyByModTimeEntries.stream()
                .findFirst());

        Optional<ReadingTypeObisCodeUsage> from = historyByJournalTimeEntries.stream()
                .findFirst();

        return to.map(ReadingTypeObisCodeUsage::getObisCode)
                .map(obisCodeTo -> {
                            return from.map(ReadingTypeObisCodeUsage::getObisCode)
                                    .filter(obisCodeFrom -> !obisCodeTo.equals(obisCodeFrom))
                                    .map(obisCodeFrom -> {
                                        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                                        auditLogChange.setName(getDisplayName(PropertyTranslationKeys.CHANNEL_OBISCODE));
                                        auditLogChange.setType(SimplePropertyType.TEXT.name());
                                        auditLogChange.setValue(obisCodeTo.toString());
                                        auditLogChange.setPreviousValue(obisCodeFrom.toString());
                                        return auditLogChange;
                                    });
                        }
                )
                .map(Optional::get);
    }

    private List<AuditLogChange> getAuditLogChangeForEndDevice() {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        DataModel dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME).get();
        DataMapper<MeterConfiguration> dataMapper = dataModel.mapper(MeterConfiguration.class);

        List<MeterConfiguration> actualEntries = getActualEntries(dataMapper, ImmutableMap.of("DEVICEID", endDevice.get().getId()));
        Optional<MeterConfiguration> fromMeterConfiguration = actualEntries.stream()
                .sorted(Comparator.comparing(mc -> mc.getInterval().toOpenRange().lowerEndpoint()))
                .findFirst();

        Optional<MeterConfiguration> toMeterConfiguration = fromMeterConfiguration.map(meterConfiguration ->
                getEntriesByInterval(dataMapper, meterConfiguration.getInterval().toOpenRange().upperEndpoint().plusMillis(1))
                        .stream()
                        .findFirst())
                .orElseGet(() -> Optional.empty());

        if (fromMeterConfiguration.isPresent() && toMeterConfiguration.isPresent()) {
            for (MeterReadingTypeConfiguration fromReadingTypeConfig : fromMeterConfiguration.get().getReadingTypeConfigs()) {
                toMeterConfiguration.get().getReadingTypeConfigs().stream()
                        .filter(to -> to.getMeasured().getMRID().compareToIgnoreCase(fromReadingTypeConfig.getMeasured().getMRID())==0)
                        .forEach(to -> {
                            if (to.getOverflowValue().get().compareTo(fromReadingTypeConfig.getOverflowValue().get())!=0) {
                                AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                                auditLogChange.setName(getDisplayName(PropertyTranslationKeys.OVERFLOW_VALUE));
                                auditLogChange.setType(SimplePropertyType.NUMBER.name());
                                auditLogChange.setValue(to.getOverflowValue().get());
                                auditLogChange.setPreviousValue(fromReadingTypeConfig.getOverflowValue().get());
                                auditLogChanges.add(auditLogChange);
                            }

                            if (to.getNumberOfFractionDigits().getAsInt() != fromReadingTypeConfig.getNumberOfFractionDigits().getAsInt() ) {
                                AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                                auditLogChange.setName(getDisplayName(PropertyTranslationKeys.NUMBER_OF_FRACTION_DIGITS));
                                auditLogChange.setType(SimplePropertyType.NUMBER.name());
                                auditLogChange.setValue(to.getNumberOfFractionDigits().getAsInt());
                                auditLogChange.setPreviousValue(fromReadingTypeConfig.getNumberOfFractionDigits().getAsInt());
                                auditLogChanges.add(auditLogChange);
                            }
                        });
            }
        }

        return auditLogChanges;
    }

    private Optional<MeterReadingTypeConfiguration> getReadingTypeConfigs(MeterReadingTypeConfiguration toReadingTypeConfig, MeterConfiguration fromMeterConfiguration){
        return fromMeterConfiguration.getReadingTypeConfigs().stream()
                .filter(fromReadingTypeConfig -> fromReadingTypeConfig.equals(toReadingTypeConfig))
                .findFirst();
    }

    private Optional<ReadingTypeObisCodeUsage> getReadingTypeObisCodeUsage(){
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        DataMapper<ReadingTypeObisCodeUsage> dataMapper = dataModel.mapper(ReadingTypeObisCodeUsage.class);

        return getActualEntries(dataMapper, getActualClauses())
                .stream()
                .findFirst()
                .map(Optional::of)
                .orElseGet(() -> getHistoryEntries(dataMapper, getHistoryByModTimeClauses(device.get().getId()))
                .stream().findFirst());
    }

    private Optional<MeterReadingTypeConfiguration> getMeterConfiguration(){
        DataModel dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME).get();
        DataMapper<MeterConfiguration> dataMapper = dataModel.mapper(MeterConfiguration.class);

        List<MeterConfiguration> actualEntries = getActualEntries(dataMapper, ImmutableMap.of("DEVICEID", endDevice.get().getId()));
        Optional<MeterConfiguration> fromMeterConfiguration = actualEntries.stream()
                .sorted(Comparator.comparing(mc -> mc.getInterval().toOpenRange().lowerEndpoint()))
                .findFirst();

        Optional<MeterConfiguration> toMeterConfiguration = fromMeterConfiguration.map(meterConfiguration ->
                getEntriesByInterval(dataMapper, meterConfiguration.getInterval().toOpenRange().upperEndpoint().plusMillis(1))
                        .stream()
                        .findFirst())
                .orElseGet(() -> Optional.empty());

        if (fromMeterConfiguration.isPresent() && toMeterConfiguration.isPresent()) {
            for (MeterReadingTypeConfiguration fromReadingTypeConfig : fromMeterConfiguration.get().getReadingTypeConfigs()) {
                Optional<MeterReadingTypeConfiguration> mrtc = toMeterConfiguration.get().getReadingTypeConfigs().stream()
                        .filter(to -> to.getMeasured().getMRID().compareToIgnoreCase(fromReadingTypeConfig.getMeasured().getMRID()) == 0)
                        .filter(to ->
                                (to.getOverflowValue().get().compareTo(fromReadingTypeConfig.getOverflowValue().get()) != 0) ||
                                        (to.getNumberOfFractionDigits().getAsInt() != fromReadingTypeConfig.getNumberOfFractionDigits().getAsInt()))
                        .findFirst();

                if (mrtc.isPresent()){
                    return mrtc;
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, Object> getActualClauses() {
        return ImmutableMap.of("DEVICEID", device.get().getId());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getEntriesByInterval(DataMapper<T> dataMapper, Instant at) {

        Condition inputCondition = Condition.TRUE;
        Condition conditionFromCurrent = inputCondition
                .and(where("METERID").isEqualTo(endDevice.get().getId()))
                .and(where("interval").isEffective(at));
        return dataMapper.select(conditionFromCurrent);
    }

    protected Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("deviceid", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    protected Map<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("deviceid", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }
}