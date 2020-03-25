package com.energyict.mdc.issue.datacollection.impl.records;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.issue.datacollection.DataCollectionEventMetadata;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public final class DataCollectionEventMetadataImpl implements DataCollectionEventMetadata, PersistenceAware {

    public enum Fields {
        EVENTYPE("eventType"),
        DEVICE("device");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private String eventType;

    private Reference<Device> device = ValueReference.absent();

    private Reference<Issue> issue = ValueReference.absent();

    private Instant createDateTime;

    private long id;

    /**
     * Audit fields
     */
    private long version;

    private Instant createTime;

    private Instant modTime;

    private String userName;

    private DataModel dataModel;

    @Inject
    public DataCollectionEventMetadataImpl(final DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public DataCollectionEventMetadataImpl init(String eventType, Device device, Issue issue, Instant createDateTime) {
        this.eventType = eventType;

        if (device == null) {
            this.device.setNull();
        } else {
            this.device.set(device);
        }

        if (issue == null) {
            this.issue.setNull();
        } else {
            this.issue.set(issue);
        }
        this.createDateTime = createDateTime;

        return this;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Device getDevice() {
        return device.or(null);
    }

    @Override
    public Issue getIssue() {
        return issue.get();
    }

    @Override
    public Instant getCreateDateTime() {
        return createDateTime;
    }

    @Override
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    @Override
    public void setDevice(final Device device) {
        if (device == null) {
            this.device.setNull();
        } else {
            this.device.set(device);
        }
    }

    @Override
    public void setIssue(final Issue issue) {
        if (issue == null) {
            this.issue.setNull();
        } else {
            this.issue.set(issue);
        }
    }

    @Override
    public void setCreateDateTime(final Instant dateTime) {
        this.createDateTime = dateTime;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataCollectionEventMetadataImpl that = (DataCollectionEventMetadataImpl) o;

        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
