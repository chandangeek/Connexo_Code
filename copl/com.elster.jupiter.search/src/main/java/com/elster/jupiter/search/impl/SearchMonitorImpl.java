package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an implementation for the {@link SearchMonitor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-08 (16:27)
 */
@Component(name = "com.elster.jupiter.search.monitor.jmx",
        property = {"jmx.objectname=com.elster.jupiter:type=SearchMonitor"})
public class SearchMonitorImpl implements SearchMonitor, SearchMonitorImplMBean {
    private ExecutionStatisticsImpl globalSearch;
    private Map<String, ExecutionStatisticsImpl> searchPerDomain = new ConcurrentHashMap<>();
    private ExecutionStatisticsImpl globalCount;
    private Map<String, ExecutionStatisticsImpl> countPerDomain = new ConcurrentHashMap<>();
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public SearchMonitorImpl() {
        super();
    }

    @Inject
    public SearchMonitorImpl(NlsService nlsService) {
        this();
        this.setNlsService(nlsService);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SearchService.COMPONENT_NAME, Layer.DOMAIN);
        this.globalSearch = new ExecutionStatisticsImpl(this.thesaurus);
        this.globalCount = new ExecutionStatisticsImpl(this.thesaurus);
    }

    @Override
    public ExecutionStatisticsImpl getSearchExecutionStatistics() {
        return this.globalSearch;
    }

    @Override
    public CompositeData getSearchExecutionStatisticsCompositeData() {
        return this.getSearchExecutionStatistics().toCompositeData();
    }

    @Override
    public ExecutionStatisticsImpl getSearchExecutionStatistics(SearchDomain searchDomain) {
        return this.getSearchExecutionStatistics(searchDomain.getId());
    }

    private ExecutionStatisticsImpl getSearchExecutionStatistics(String searchDomainId) {
        return this.searchPerDomain.computeIfAbsent(searchDomainId, id -> new ExecutionStatisticsImpl(thesaurus));
    }

    @Override
    public CompositeData getSearchExecutionStatisticsCompositeData(String searchDomainId) {
        return this.getSearchExecutionStatistics(searchDomainId).toCompositeData();
    }

    @Override
    public void searchExecuted(SearchDomain searchDomain, long nanos) {
        synchronized (this.globalSearch) {
            this.globalSearch.registerExecution(nanos);
            this.getSearchExecutionStatistics(searchDomain).registerExecution(nanos);
        }
    }

    @Override
    public ExecutionStatisticsImpl getCountExecutionStatistics() {
        return this.globalCount;
    }

    @Override
    public CompositeData getCountExecutionStatisticsCompositeData() {
        return this.getCountExecutionStatistics().toCompositeData();
    }

    @Override
    public ExecutionStatisticsImpl getCountExecutionStatistics(SearchDomain searchDomain) {
        return this.getCountExecutionStatistics(searchDomain.getId());
    }

    private ExecutionStatisticsImpl getCountExecutionStatistics(String searchDomainId) {
        return this.countPerDomain.computeIfAbsent(searchDomainId, id -> new ExecutionStatisticsImpl(thesaurus));
    }

    @Override
    public CompositeData getCountExecutionStatisticsCompositeData(String searchDomainId) {
        return this.getCountExecutionStatistics(searchDomainId).toCompositeData();
    }

    @Override
    public void countExecuted(SearchDomain searchDomain, long nanos) {
        synchronized (this.globalCount) {
            this.globalCount.registerExecution(nanos);
            this.getCountExecutionStatistics(searchDomain).registerExecution(nanos);
        }
    }

    private static class ExecutionStatisticsImpl implements ExecutionStatistics {
        private static final String COUNT_ITEM_NAME = "executionCount";
        private static final String COUNT_ITEM_DESCRIPTION = "Number of executions";
        private static final String TOTAL_NANOS_ITEM_NAME = "totalNanos";
        private static final String TOTAL_NANOS_ITEM_DESCRIPTION = "Total amount of nanos spent on all executions";
        private static final String AVERAGE_NANOS_ITEM_NAME = "averageNanos";
        private static final String AVERAGE_NANOS_ITEM_DESCRIPTION = "Average amount of nanos spent on all executions";
        private static final String MINIMUM_NANOS_ITEM_NAME = "minimumNanos";
        private static final String MINIMUM_NANOS_ITEM_DESCRIPTION = "Amount of nanos spent on fastest execution";
        private static final String MAXIMUM_NANOS_ITEM_NAME = "maximumNanos";
        private static final String MAXIMUM_NANOS_ITEM_DESCRIPTION = "Amount of nanos spent on slowest execution";

        private long count = 0;
        private long totalNanos = 0;
        private long minimumNanos = Long.MAX_VALUE;
        private long maximumNanos = Long.MIN_VALUE;
        private final Thesaurus thesaurus;
        private List<CompositeDataItemAccessor> accessors;
        private String[] accessorNames;

        private ExecutionStatisticsImpl(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public synchronized long getCount() {
            return this.count;
        }

        @Override
        public synchronized long getTotal() {
            return this.totalNanos;
        }

        @Override
        public synchronized long getMinimum() {
            return this.minimumNanos;
        }

        @Override
        public synchronized long getMaximum() {
            return this.maximumNanos;
        }

        @Override
        public synchronized long getAverage() {
            if (this.count > 0) {
                return this.totalNanos / this.count;
            } else {
                return this.totalNanos;
            }
        }

        private void registerExecution(long nanos) {
            this.count++;
            this.totalNanos = this.totalNanos + nanos;
            this.minimumNanos = Long.min(this.minimumNanos, nanos);
            this.maximumNanos = Long.max(this.maximumNanos, nanos);
        }

        private synchronized CompositeData toCompositeData() {
            this.ensureAccessors();
            try {
                return new CompositeDataSupport(
                        this.getCompositeType(),
                        this.accessorNames,
                        this.values());
            }
            catch (OpenDataException e) {
                throw new UnexpectedOpenDataException(this.thesaurus, MessageSeeds.COMPOSITE_TYPE_CREATION, e);
            }
        }

        private CompositeType getCompositeType () {
            try {
                return new CompositeType(
                        SearchMonitorImpl.class.getSimpleName(),
                        "Operational statistics",
                        this.itemNames(),
                        this.itemDescriptions(),
                        this.itemTypes());
            }
            catch (OpenDataException e) {
                throw new UnexpectedOpenDataException(this.thesaurus, MessageSeeds.COMPOSITE_TYPE_CREATION, e);
            }
        }

        private String[] itemNames () {
            return new String[] {
                    COUNT_ITEM_NAME,
                    TOTAL_NANOS_ITEM_NAME,
                    AVERAGE_NANOS_ITEM_NAME,
                    MINIMUM_NANOS_ITEM_NAME,
                    MAXIMUM_NANOS_ITEM_NAME};
        }

        private String[] itemDescriptions () {
            return new String[] {
                    COUNT_ITEM_DESCRIPTION,
                    TOTAL_NANOS_ITEM_DESCRIPTION,
                    AVERAGE_NANOS_ITEM_DESCRIPTION,
                    MINIMUM_NANOS_ITEM_DESCRIPTION,
                    MAXIMUM_NANOS_ITEM_DESCRIPTION};
        }

        private OpenType[] itemTypes () {
            return new OpenType[] {
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.LONG,
                    SimpleType.LONG};
        }

        private Object[] values () {
            this.ensureAccessors();
            Object[] values = new Object[this.accessors.size()];
            int valueIndex = 0;
            for (CompositeDataItemAccessor accessor : this.accessors) {
                values[valueIndex] = accessor.getValue();
                valueIndex++;
            }
            return values;
        }

        /**
         * Ensures that the accessors map is initialize properly,
         * i.e. that all CompositeData items have an accessor.
         */
        private void ensureAccessors () {
            if (this.accessors == null) {
                this.accessors = new ArrayList<>();
                this.initializeAccessors(this.accessors);
                this.accessorNames = new String[this.accessors.size()];
                int accessorNameIndex = 0;
                for (CompositeDataItemAccessor accessor : this.accessors) {
                    this.accessorNames[accessorNameIndex] = accessor.getItemName();
                    accessorNameIndex++;
                }
            }
        }

        private void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
            accessors.add(new CompositeDataItemAccessor(COUNT_ITEM_NAME, this::getCount));
            accessors.add(new CompositeDataItemAccessor(TOTAL_NANOS_ITEM_NAME, this::getTotal));
            accessors.add(new CompositeDataItemAccessor(AVERAGE_NANOS_ITEM_NAME, this::getAverage));
            accessors.add(new CompositeDataItemAccessor(MINIMUM_NANOS_ITEM_NAME, this::getMinimum));
            accessors.add(new CompositeDataItemAccessor(MAXIMUM_NANOS_ITEM_NAME, this::getMaximum));
        }

    }

    private interface ValueProvider {
        Object getValue ();
    }

    private static class CompositeDataItemAccessor implements ValueProvider {
        private String itemName;
        private ValueProvider valueProvider;

        private CompositeDataItemAccessor (String itemName, ValueProvider valueProvider) {
            super();
            this.itemName = itemName;
            this.valueProvider = valueProvider;
        }

        public String getItemName () {
            return itemName;
        }

        @Override
        public Object getValue () {
            return this.valueProvider.getValue();
        }

    }

}