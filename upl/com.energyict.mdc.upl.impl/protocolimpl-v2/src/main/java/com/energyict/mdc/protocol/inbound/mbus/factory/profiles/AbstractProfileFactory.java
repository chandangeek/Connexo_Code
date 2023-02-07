package com.energyict.mdc.protocol.inbound.mbus.factory.profiles;

import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.mdc.protocol.inbound.mbus.InboundContext;
import com.energyict.mdc.protocol.inbound.mbus.factory.AbstractMerlinFactory;
import com.energyict.mdc.protocol.inbound.mbus.factory.UnitFactory;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.Telegram;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.SpacingControlByte;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body.TelegramVariableDataRecord;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.MeasureUnit;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.VIFUnitMultiplierMasks;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractProfileFactory extends AbstractMerlinFactory {

    private static final int DEFAULT_PROTOCOL_STATUS = 0;
    private static final int DEFAULT_EI_STATUS = 0;

    private VIFUnitMultiplierMasks fieldType;
    private MeasureUnit measureUnit;
    private int multiplier;
    private long startIndex;
    private Instant midnight;
    private long calculationIndex;
    private Instant calculationTimeStamp;

    private CollectedLoadProfile loadProfile;
    private ArrayList<ChannelInfo> loadProfileChannels;
    private List<IntervalData> collectedIntervalData;


    public AbstractProfileFactory(Telegram telegram, InboundContext inboundContext) {
        super(telegram, inboundContext);
    }

    public abstract SpacingControlByte applicableSpacingControlByte();

    //each implementation must get its own CIM code
    public abstract String getReadingTypeMRID();

    private Duration getLoadProfileInterval() {
        return Duration.of(applicableSpacingControlByte().getTimeAmount(), applicableSpacingControlByte().getChronoUnit());
    }


    public boolean appliesFor(TelegramVariableDataRecord record) {
        try {
            return (applicableDataFieldEncoding().equals(record.getDif().getDataFieldEncoding())
                    && applicableFunctionType().equals(record.getDif().getFunctionType()))
                    && Integer.parseInt(record.getDataField().getFieldParts().get(1), 16) == applicableSpacingControlByte().getValue()
                    && Integer.parseInt(record.getDataField().getFieldParts().get(2), 16) == applicableSpacingControlByte().getTimeAmount();
        } catch (Exception ex) {
            getInboundContext().getLogger().error("Error while checking applicability of record", ex);
            ex.printStackTrace();
            return false;
        }
    }

    public boolean appliesAsIndexRecord(TelegramVariableDataRecord indexRecord) {
        if (getFieldType() == null || getMeasureUnit() == null) {
            return false;
        }

        return TelegramFunctionType.INSTANTANEOUS_VALUE.equals(indexRecord.getDif().getFunctionType())
                && TelegramEncoding.ENCODING_INTEGER.equals(indexRecord.getDif().getDataFieldEncoding())
                && getFieldType().equals(indexRecord.getVif().getType())
                && getMeasureUnit().equals(indexRecord.getVif().getmUnit());
    }

    public TelegramEncoding applicableDataFieldEncoding() {
        return TelegramEncoding.ENCODING_VARIABLE_LENGTH;
    }

    public TelegramFunctionType applicableFunctionType() {
        return TelegramFunctionType.INSTANTANEOUS_VALUE;
    }

    private void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    private void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
    }

    private void setFieldType(VIFUnitMultiplierMasks type) {
        this.fieldType = type;
    }

    public VIFUnitMultiplierMasks getFieldType() {
        return fieldType;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public CollectedLoadProfile getCollectedLoadProfile(){
        return this.loadProfile;
    }

    public void extractProfileMetaData(TelegramVariableDataRecord record) {
        setFieldType(record.getVif().getType());
        setMeasureUnit(record.getVif().getmUnit());
        setMultiplier(record.getVif().getMultiplier());
        extractTelegramDateTime();
    }


    public CollectedLoadProfile extractLoadProfile(TelegramVariableDataRecord profileRecord, final TelegramVariableDataRecord indexRecord) {
        if (!appliesFor(profileRecord)) {
            getInboundContext().getLogger().debug(this.getClass().getSimpleName() + ": Record not applicable to " + profileRecord);
            return null;
        }

        extractProfileMetaData(profileRecord);

        if (!appliesAsIndexRecord(indexRecord)) {
            getInboundContext().getLogger().debug(this.getClass().getSimpleName() + ": Invalid index record, it doesn't match the profile record " + profileRecord);
            return null;
        }

        getInboundContext().getLogger().info(this.getClass().getSimpleName() + ": Decoding " + profileRecord + " as profile in " + getObisCode());

        setupCollectedProfile(profileRecord);
        setStartIndex(indexRecord);
        calculateLoadProfile(profileRecord);

        return getCollectedLoadProfile();
    }

    /**
     * Prepare all the shitty core collected details ...
     */
    private void setupCollectedProfile(TelegramVariableDataRecord profileRecord) {
        // create a single channel
        int id = 0;
        String name = getObisCode().toString();
        Unit unit = UnitFactory.from(profileRecord, getInboundContext());

        String meterIdentification = getDeviceIdentifier().toString();
        boolean cumulative = false;
        String readingTypeMRID = getReadingTypeMRID();

        ChannelInfo ch1 = new ChannelInfo(id, name, unit, meterIdentification, cumulative, readingTypeMRID);
        this.loadProfileChannels = new ArrayList<>();
        loadProfileChannels.add(ch1);

        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(getObisCode(), getDeviceIdentifier());

        this.loadProfile = getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
        this.collectedIntervalData = new ArrayList<>();

    }



    private void setStartIndex(TelegramVariableDataRecord indexRecord) {
        this.startIndex = Long.parseLong(indexRecord.getDataField().getParsedValue());
        this.midnight = toMidnightWithTimeZone(getTelegramDateTime(), getTimeZone());
        getInboundContext().getLogger().info(this.getClass().getSimpleName() + " Starting load profile calculation backwards from midnight: " + midnight + ", in time-zone " + getTimeZone()) ;
    }

    private ZoneId getTimeZone() {
        return getInboundContext().getTimeZone();
    }


    private void calculateLoadProfile(TelegramVariableDataRecord record) {
        startCalculation(getStartIndex(), getStartReferenceTimeStamp());

        Map<Integer, Long> intervals = record.getDataField().getParsedIntervals();
        intervals.keySet()
                .forEach(id -> {
                    Long value = intervals.get(id);
                    calculateInterval(value);
                });

        endCalculation();

    }

    /** From where to start the load profile calculations */
    protected Instant getStartReferenceTimeStamp() {
        return getMidnight();
    }

    public Instant getMidnight() {
        return midnight;
    }

    private void startCalculation(long startIndex, Instant startDateTime) {
        this.calculationIndex = startIndex;
        this.calculationTimeStamp = startDateTime;
        saveCurrentInterval();
    }

    private void calculateInterval(Long value) {
        // the indexes are "signed differences" (from the last? index I suppose)
        calculationIndex = calculationIndex - value;
        calculationTimeStamp = calculationTimeStamp.minus(applicableSpacingControlByte().getTimeAmount(), applicableSpacingControlByte().getChronoUnit());

        saveCurrentInterval();
    }

    private void endCalculation() {
        loadProfile.setCollectedIntervalData(collectedIntervalData, loadProfileChannels);
    }

    private void saveCurrentInterval() {
        getInboundContext().getLogger().info(this.getClass().getSimpleName() + " " + calculationTimeStamp.toString() + " = " + calculationIndex);
        IntervalValue intervalValue = new IntervalValue(calculationIndex, DEFAULT_PROTOCOL_STATUS, DEFAULT_EI_STATUS);

        IntervalData interval = new IntervalData(Date.from(calculationTimeStamp));
        List<IntervalValue> intervalValues = new ArrayList<>();
        intervalValues.add(intervalValue);
        interval.setIntervalValues(intervalValues);
        collectedIntervalData.add(interval);
    }


}
