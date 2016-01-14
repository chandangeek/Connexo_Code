package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import java.time.Clock;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.monitor.ComServerOperationalStatistics;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.List;

/**
 * Provides an implementation for the {@link ComServerOperationalStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (17:59)
 */
public class ComServerOperationalStatisticsImpl extends OperationalStatisticsImpl implements ComServerOperationalStatistics {

    public static final String SERVER_LOG_LEVEL_ITEM_NAME = "serverLogLevel";
    private static final String SERVER_LOG_LEVEL_ITEM_DESCRIPTION = "server log level";
    public static final String COMMUNICATION_LOG_LEVEL_ITEM_NAME = "communicationLogLevel";
    private static final String COMMUNICATION_LOG_LEVEL_ITEM_DESCRIPTION = "communication log level";

    private RunningComServer runningComServer;

    public ComServerOperationalStatisticsImpl(RunningComServer runningComServer, Clock clock, Thesaurus thesaurus) {
        super(clock, thesaurus, runningComServer.getComServer().getChangesInterPollDelay());
        this.runningComServer = runningComServer;
    }

    @Override
    public ComServer.LogLevel getServerLogLevel () {
        return this.runningComServer.getComServer().getServerLogLevel();
    }

    private String getServerLogLevelString () {
        return getServerLogLevel().toString();
    }

    @Override
    public ComServer.LogLevel getCommunicationLogLevel () {
        return this.runningComServer.getComServer().getCommunicationLogLevel();
    }

    private String getCommunicationLogLevelString () {
        return getCommunicationLogLevel().toString();
    }


    @Override
    protected void addItemNames (List<String> itemNames) {
        super.addItemNames(itemNames);
        itemNames.add(SERVER_LOG_LEVEL_ITEM_NAME);
        itemNames.add(COMMUNICATION_LOG_LEVEL_ITEM_NAME);
    }

    @Override
    protected void addItemDescriptions (List<String> itemDescriptions) {
        super.addItemDescriptions(itemDescriptions);
        itemDescriptions.add(SERVER_LOG_LEVEL_ITEM_DESCRIPTION);
        itemDescriptions.add(COMMUNICATION_LOG_LEVEL_ITEM_DESCRIPTION);
    }

    @Override
    protected void addItemTypes (List<OpenType> itemTypes) {
        super.addItemTypes(itemTypes);
        itemTypes.add(SimpleType.STRING);
        itemTypes.add(SimpleType.STRING);
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        super.initializeAccessors(accessors);
        accessors.add( new CompositeDataItemAccessor(SERVER_LOG_LEVEL_ITEM_NAME, this::getServerLogLevelString));
        accessors.add( new CompositeDataItemAccessor(COMMUNICATION_LOG_LEVEL_ITEM_NAME, this::getCommunicationLogLevelString));
    }

}