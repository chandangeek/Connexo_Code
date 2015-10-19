package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

public class UsagePointMetrologyConfigurationImpl implements UsagePointMetrologyConfiguration {
	
	private long id;
	private Interval interval;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    
    private Reference<UsagePoint> usagePoint = ValueReference.absent();
	private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    
    private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;
    
    @Inject
    UsagePointMetrologyConfigurationImpl(Clock clock, DataModel dataModel, EventService eventService) {
    	this.clock = clock;
    	this.dataModel = dataModel;
    	this.eventService = eventService;
    }
    
	UsagePointMetrologyConfigurationImpl init(UsagePoint UsagePoint , MetrologyConfiguration metrologyConfig , Interval interval) {
		this.usagePoint.set(Objects.requireNonNull(UsagePoint));
		this.metrologyConfiguration.set(Objects.requireNonNull(metrologyConfig));
		this.interval = Objects.requireNonNull(interval);
		return this;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public UsagePoint getUsagePoint() {
		return usagePoint.get();
	}
		
	@Override
	public MetrologyConfiguration getMetrologyConfiguration() {
		return metrologyConfiguration.get();
	}

	@Override
	public boolean isCurrent() {
		return getRange().contains(clock.instant());
	}

	@Override
	public Interval getInterval() {
        return interval;
    }

    @Override
    public void update() {
        Save s = action(getId());
        s.save(dataModel, this);
        if (s == Save.CREATE) {
            eventService.postEvent(EventType.USAGEPOINTMETROLOGYCONFIGURATION_CREATED.topic(), this);
        } else {
            eventService.postEvent(EventType.USAGEPOINTMETROLOGYCONFIGURATION_UPDATED.topic(), this);            
        }
    }
    
    @Override
    public void delete() {
        dataModel.remove(this);
        eventService.postEvent(EventType.USAGEPOINTMETROLOGYCONFIGURATION_DELETED.topic(), this);  
    }
    
    @Override
    public boolean conflictsWith(UsagePointMetrologyConfiguration other) {
        return getUsagePoint().getId() == other.getUsagePoint().getId() && 
                other.getRange().isConnected(getRange()) &&
                !other.getRange().intersection(getRange()).isEmpty();
    }

    void terminate(Instant date) {
        if (!isEffectiveAt(date)) {
            throw new IllegalArgumentException();
        }
        interval = Interval.of(Range.atLeast(date));
    }

    @Override
    public String toString() {
    	return toStringHelper(this).add("UsagePoint", usagePoint).add("metrologyConfiguration", metrologyConfiguration).add("interval", interval).toString();
    }

    @Override
    public long getVersion() {
        return version;
    }

    public String getUserName() {
        return userName;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UsagePointMetrologyConfigurationImpl)) {
            return false;
        }

        UsagePointMetrologyConfigurationImpl that = (UsagePointMetrologyConfigurationImpl) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
