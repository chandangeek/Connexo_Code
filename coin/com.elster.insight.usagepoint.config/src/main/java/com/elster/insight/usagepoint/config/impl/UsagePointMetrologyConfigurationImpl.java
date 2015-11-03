package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

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

//@Unique(fields = "usagePoint.id", groups = {Save.Create.class, Save.Update.class})
public class UsagePointMetrologyConfigurationImpl implements UsagePointMetrologyConfiguration {
    private long id;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private Reference<UsagePoint> usagePoint = ValueReference.absent();
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    UsagePointMetrologyConfigurationImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }

    UsagePointMetrologyConfigurationImpl init(UsagePoint UsagePoint, MetrologyConfiguration metrologyConfig) {
        this.usagePoint.set(Objects.requireNonNull(UsagePoint));
        this.metrologyConfiguration.set(Objects.requireNonNull(metrologyConfig));
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
    public void updateMetrologyConfiguration(MetrologyConfiguration mc) {
        this.metrologyConfiguration.set(mc);
        this.update();
    }

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
    public String toString() {
        return toStringHelper(this).add("UsagePoint", usagePoint).add("metrologyConfiguration", metrologyConfiguration).toString();
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

        return getUsagePoint().getId() == that.getUsagePoint().getId();

    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsagePoint().getId());
    }
}
