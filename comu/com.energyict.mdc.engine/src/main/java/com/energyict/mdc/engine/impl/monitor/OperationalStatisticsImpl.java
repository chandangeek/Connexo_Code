package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import org.joda.time.DateTimeConstants;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link OperationalStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (14:30)
 */
public class OperationalStatisticsImpl extends CanConvertToCompositeDataSupport implements OperationalStatistics {

    public static final String START_TIMESTAMP_ITEM_NAME = "startTimeStamp";
    private static final String START_TIMESTAMP_ITEM_DESCRIPTION = "start timestamp";
    public static final String RUNNING_TIME_ITEM_NAME = "runningTime";
    private static final String RUNNING_TIME_ITEM_DESCRIPTION = "running time";
    public static final String CHANGES_INTERPOLL_DELAY_ITEM_NAME = "changesInterPollDelay";
    private static final String CHANGES_INTERPOLL_DELAY_ITEM_DESCRIPTION = "changes inter poll delay";
    public static final String LAST_CHECK_FOR_CHANGES_ITEM_NAME = "lastCheckForChangesTimestamp";
    private static final String LAST_CHECK_FOR_CHANGES_ITEM_DESCRIPTION = "last check for changes timestamp";

    private final Clock clock;
    private final Thesaurus thesaurus;
    private final Date startTimestamp;
    private final TimeDuration changesInterPollDelay;
    private Date lastCheckForChangesTimestamp;

    public OperationalStatisticsImpl(Clock clock, Thesaurus thesaurus, TimeDuration changesInterPollDelay) {
        super();
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.startTimestamp = Date.from(this.clock.instant());
        this.changesInterPollDelay = changesInterPollDelay;
    }

    @Override
    public Date getStartTimestamp () {
        return startTimestamp;
    }

    @Override
    public TimeDuration getRunningTime () {
        Instant now = this.clock.instant();
        return TimeDuration.seconds(this.asSeconds(now.toEpochMilli() - this.startTimestamp.getTime()));
    }

    private int asSeconds (long millis) {
        return (int) millis / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public TimeDuration getChangesInterPollDelay () {
        return this.changesInterPollDelay;
    }

    @Override
    public Optional<Date> getLastCheckForChangesTimestamp() {
        return Optional.ofNullable(this.lastCheckForChangesTimestamp);
    }

    @Override
    public void setLastCheckForChangesTimestamp (Date lastCheckForChangesTimestamp) {
        this.lastCheckForChangesTimestamp = lastCheckForChangesTimestamp;
    }

    public CompositeType getCompositeType () {
        try {
            return new CompositeType(
                    this.getClass().getSimpleName(),
                    "Operational statistics",
                    this.itemNames(),
                    this.itemDescriptions(),
                    this.itemTypes());
        }
        catch (OpenDataException e) {
            throw CodingException.compositeTypeCreation(this.getClass(), e, MessageSeeds.COMPOSITE_TYPE_CREATION);
        }
    }

    private String[] itemNames () {
        List<String> itemNames = new ArrayList<>();
        this.addItemNames(itemNames);
        return itemNames.toArray(new String[itemNames.size()]);
    }

    protected void addItemNames (List<String> itemNames) {
        itemNames.add(START_TIMESTAMP_ITEM_NAME);
        itemNames.add(RUNNING_TIME_ITEM_NAME);
        itemNames.add(CHANGES_INTERPOLL_DELAY_ITEM_NAME);
        itemNames.add(LAST_CHECK_FOR_CHANGES_ITEM_NAME);
    }

    private String[] itemDescriptions () {
        List<String> itemDescriptions = new ArrayList<>();
        this.addItemDescriptions(itemDescriptions);
        return itemDescriptions.toArray(new String[itemDescriptions.size()]);
    }

    protected void addItemDescriptions (List<String> itemDescriptions) {
        itemDescriptions.add(START_TIMESTAMP_ITEM_DESCRIPTION);
        itemDescriptions.add(RUNNING_TIME_ITEM_DESCRIPTION);
        itemDescriptions.add(CHANGES_INTERPOLL_DELAY_ITEM_DESCRIPTION);
        itemDescriptions.add(LAST_CHECK_FOR_CHANGES_ITEM_DESCRIPTION);
    }

    private OpenType[] itemTypes () {
        List<OpenType> itemTypes = new ArrayList<>();
        this.addItemTypes(itemTypes);
        return itemTypes.toArray(new OpenType[itemTypes.size()]);
    }

    protected void addItemTypes (List<OpenType> itemTypes) {
        itemTypes.add(SimpleType.DATE);
        itemTypes.add(SimpleType.STRING);
        itemTypes.add(SimpleType.STRING);
        itemTypes.add(SimpleType.DATE);
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        accessors.add(
                new CompositeDataItemAccessor(
                        START_TIMESTAMP_ITEM_NAME,
                        this::getStartTimestamp));
        accessors.add(
                new CompositeDataItemAccessor(
                        RUNNING_TIME_ITEM_NAME,
                        () -> new PrettyPrintTimeDuration(getRunningTime(), thesaurus).toString()));
        accessors.add(
                new CompositeDataItemAccessor(
                        CHANGES_INTERPOLL_DELAY_ITEM_NAME,
                        () -> new PrettyPrintTimeDuration(getChangesInterPollDelay(), thesaurus).toString()));
        accessors.add(
                new CompositeDataItemAccessor(
                        LAST_CHECK_FOR_CHANGES_ITEM_NAME,
                        () -> getLastCheckForChangesTimestamp().orElse(null)));
    }

}