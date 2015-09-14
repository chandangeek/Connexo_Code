package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.core.RunningComServer;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.List;

/**
 * Provides an implementation for the {@link CollectedDataStorageStatistics} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-05 (13:16)
 */
public class CollectedDataStorageStatisticsImpl extends CanConvertToCompositeDataSupport implements CollectedDataStorageStatistics {

    public static final String CAPACITY_ITEM_NAME = "capacity";
    private static final String CAPACITY_ITEM_DESCRIPTION = "capacity";
    public static final String SIZE_ITEM_NAME = "size";
    private static final String SIZE_ITEM_DESCRIPTION = "current size";
    public static final String LOAD_ITEM_PERCENTAGE_NAME = "loadPercentage";
    private static final String LOAD_PERCENTAGE_ITEM_DESCRIPTION = "load as percentage";
    public static final String NUMBER_OF_THREADS_ITEM_NAME = "numberOfThreads";
    private static final String NUMBER_OF_THREADS_ITEM_DESCRIPTION = "number of threads";
    public static final String THREAD_PRIORITY_ITEM_NAME = "threadPriority";
    private static final String THREAD_PRIORITY_ITEM_DESCRIPTION = "thread priority";

    private RunningComServer comServer;

    public CollectedDataStorageStatisticsImpl (RunningComServer comServer) {
        super();
        this.comServer = comServer;
    }

    @Override
    public int getCapacity () {
        return this.comServer.getCollectedDataStorageCapacity();
    }

    @Override
    public int getCurrentSize () {
        return this.comServer.getCurrentCollectedDataStorageSize();
    }

    @Override
    public int getLoadPercentage () {
        return this.comServer.getCurrentCollectedDataStorageLoadPercentage();
    }

    @Override
    public int getNumberOfThreads () {
        return this.comServer.getNumberOfCollectedDataStorageThreads();
    }

    @Override
    public int getThreadPriority () {
        return this.comServer.getCollectedDataStorageThreadPriority();
    }

    public CompositeType getCompositeType () {
        return classCompositeType(this.getClass());
    }

    private static CompositeType classCompositeType (Class targetClass) {
        try {
            return new CompositeType(
                    targetClass.getSimpleName(),
                    "Collected data storage statistics",
                    new String[]{CAPACITY_ITEM_NAME, SIZE_ITEM_NAME, LOAD_ITEM_PERCENTAGE_NAME, NUMBER_OF_THREADS_ITEM_NAME, THREAD_PRIORITY_ITEM_NAME},
                    new String[]{CAPACITY_ITEM_DESCRIPTION, SIZE_ITEM_DESCRIPTION, LOAD_PERCENTAGE_ITEM_DESCRIPTION, NUMBER_OF_THREADS_ITEM_DESCRIPTION, THREAD_PRIORITY_ITEM_DESCRIPTION},
                    new OpenType[]{
                            SimpleType.INTEGER,
                            SimpleType.INTEGER,
                            SimpleType.INTEGER,
                            SimpleType.INTEGER,
                            SimpleType.INTEGER});
        }
        catch (OpenDataException e) {
            throw CodingException.compositeTypeCreation(targetClass, e, MessageSeeds.COMPOSITE_TYPE_CREATION);
        }
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        accessors.add(
                new CompositeDataItemAccessor(CAPACITY_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getCapacity();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(SIZE_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getCurrentSize();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(LOAD_ITEM_PERCENTAGE_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getLoadPercentage();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(NUMBER_OF_THREADS_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getNumberOfThreads();
                    }
                }));
        accessors.add(
                new CompositeDataItemAccessor(THREAD_PRIORITY_ITEM_NAME, new ValueProvider() {
                    @Override
                    public Object getValue () {
                        return getThreadPriority();
                    }
                }));
    }

}