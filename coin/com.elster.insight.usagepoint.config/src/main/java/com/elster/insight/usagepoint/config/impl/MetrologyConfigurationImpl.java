package com.elster.insight.usagepoint.config.impl;

import static com.elster.jupiter.domain.util.Save.action;
import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

import javax.inject.Inject;
import javax.validation.constraints.Size;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.Unique;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

@Unique(fields="name", groups = Save.Create.class)
public class MetrologyConfigurationImpl implements MetrologyConfiguration {
    private long id;
    private long version;

    @NotEmpty    
    @Size(max=80)
    private String name;
    
    private final DataModel dataModel;
    private final EventService eventService;
    
    @Inject
    MetrologyConfigurationImpl(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
    }
    
    MetrologyConfigurationImpl init(String name) {
        if (name != null) {
            setName(name.trim());
        }
        this.save();
        return this;
    }
    
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void save() {
        Save s = action(getId());
        s.save(dataModel, this);
        if (s == Save.CREATE) {
            eventService.postEvent(EventType.METROLOGYCONFIGURATION_CREATED.topic(), this);
        } else {
            eventService.postEvent(EventType.METROLOGYCONFIGURATION_UPDATED.topic(), this);            
        }
    }

    @Override
    public void delete() {
        dataModel.remove(this);
        eventService.postEvent(EventType.METROLOGYCONFIGURATION_DELETED.topic(), this);  
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetrologyConfiguration)) {
            return false;
        }
        MetrologyConfiguration party = (MetrologyConfiguration) o;
        return id == party.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return toStringHelper(this).omitNullValues().add("id", id).add("name", name).toString();
    }
}
