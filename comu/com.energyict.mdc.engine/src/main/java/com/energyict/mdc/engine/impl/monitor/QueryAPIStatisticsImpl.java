package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.tools.JmxStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.monitor.QueryAPIStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-05 (13:16)
 */
public class QueryAPIStatisticsImpl extends CanConvertToCompositeDataSupport implements QueryAPIStatistics {

    public static final String NUMBER_OF_CLIENTS_ITEM_NAME = "numberOfClients";
    private static final String NUMBER_OF_CLIENTS_ITEM_DESCRIPTION = "number of clients";
    public static final String NUMBER_OF_FAILURES_ITEM_NAME = "numberOfFailures";
    private static final String NUMBER_OF_FAILURES_ITEM_DESCRIPTION = "number of failures";
    public static final String CALL_STATISTICS_ITEM_NAME = "callStatistics";
    private static final String CALL_STATISTICS_ITEM_DESCRIPTION = "call statistics";

    private int numberOfClients;
    private int numberOfFailures;
    private JmxStatistics callStatistics;
    private String comServerName;
    private Map<String, Date> registeredClientsSince = new HashMap<>();

    public QueryAPIStatisticsImpl(ComServer comServer) {
        super();
        this.comServerName = comServer.getName();
        this.callStatistics = new JmxStatistics(this.comServerName);
    }

    @Override
    public void reset () {
        this.numberOfFailures = 0;
        this.callStatistics = new JmxStatistics(this.comServerName);
    }

    @Override
    public int getNumberOfClients () {
        return numberOfClients;
    }

    public void setNumberOfClients (int numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public int getNumberOfFailures () {
        return numberOfFailures;
    }

    public void setNumberOfFailures (int numberOfFailures) {
        this.numberOfFailures = numberOfFailures;
    }

    @Override
    public Map<String, Date> getRegisteredClients() {
        return registeredClientsSince;
    }

    @Override
    public synchronized void clientRegistered (String clientName, Date lastSeen ) {
        /* Make sure this is properly guarded because clients
         * register via the internal jetty server
         * and that uses or may use different threads
         * for every http request. */
        this.numberOfClients++;
        registeredClientsSince.put(clientName, lastSeen);
    }

    @Override
    public synchronized void clientUnregistered (String clientName) {
        /* Make sure this is properly guarded because clients
         * register via the internal jetty server
         * and that uses or may use different threads
         * for every http request. */
        this.numberOfClients--;
        /* as we still want to see when the client connected the last time,
        * we do not remove the entree from the registeredClientSince map
        */
    }

    @Override
    public synchronized void callCompleted (long duration) {
        this.callStatistics.update(duration);
    }

    @Override
    public void callFailed (long duration) {
        this.callStatistics.update(duration);
        this.numberOfFailures++;
    }

    @Override
    public JmxStatistics getCallStatistics () {
        return callStatistics;
    }

    public CompositeType getCompositeType () {
        return classCompositeType(this.getClass());
    }

    public static CompositeType classCompositeType (Class targetClass) {
        try {
            return new CompositeType(
                    targetClass.getSimpleName(),
                    "Query API Statistics",
                    new String[]{NUMBER_OF_CLIENTS_ITEM_NAME, NUMBER_OF_FAILURES_ITEM_NAME, CALL_STATISTICS_ITEM_NAME},
                    new String[]{NUMBER_OF_CLIENTS_ITEM_DESCRIPTION, NUMBER_OF_FAILURES_ITEM_DESCRIPTION, CALL_STATISTICS_ITEM_DESCRIPTION},
                    new OpenType[] {
                            SimpleType.INTEGER,
                            SimpleType.INTEGER,
                            JmxStatistics.doGetCompositeType()});
        }
        catch (OpenDataException e) {
            throw CodingException.compositeTypeCreation(targetClass, e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        accessors.add(
                new CompositeDataItemAccessor(NUMBER_OF_CLIENTS_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getNumberOfClients();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(NUMBER_OF_FAILURES_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getNumberOfFailures();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(CALL_STATISTICS_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        try {
                            return getCallStatistics().getSupport();
                        }
                        catch (OpenDataException e) {
                            throw CodingException.compositeDataCreation(JmxStatistics.class, e, MessageSeeds.JSON_PARSING_ERROR);
                        }
                    }
                }));
    }

}