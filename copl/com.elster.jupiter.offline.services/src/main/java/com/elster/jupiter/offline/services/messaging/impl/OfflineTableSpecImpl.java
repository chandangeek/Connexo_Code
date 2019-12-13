/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.messaging.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;


public class OfflineTableSpecImpl implements QueueTableSpec {

    // persistent fields
    @Size(max = 24, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.QUEUE_NAME_TOO_LONG + "}")
    private String name;
    private String payloadType;
    private boolean multiConsumer;
    private boolean active;
    private boolean isPrioritized;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private transient boolean fromDB = true;
    private final Thesaurus thesaurus;

    @Inject
    OfflineTableSpecImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    OfflineTableSpecImpl init(String name, String payloadType, boolean multiConsumer, boolean isPrioritized) {
        this.name = name;
        this.payloadType = payloadType;
        this.multiConsumer = multiConsumer;
        this.isPrioritized = isPrioritized;
        this.fromDB = false;
        return this;
    }

    public static OfflineTableSpecImpl from(DataModel dataModel, String name, String payloadType, boolean multiConsumer) {
        return dataModel.getInstance(OfflineTableSpecImpl.class).init(name, payloadType, multiConsumer, false);
    }

    public static OfflineTableSpecImpl from(DataModel dataModel, String name, String payloadType, boolean multiConsumer, boolean isPrioritized) {
        return dataModel.getInstance(OfflineTableSpecImpl.class).init(name, payloadType, multiConsumer, isPrioritized);
    }

    @Override
    public void activate() {
        if (isActive()) {
            return;
        }
        if (isJms()) {
            doActivateJms();
        } else {
        }
        active = true;
        dataModel.mapper(QueueTableSpec.class).update(this, "active");
    }

    private Connection getConnection() throws SQLException {
        return dataModel.getConnection(false);
    }

    private void doActivateJms() {
    }

    @Override
    public void deactivate() {
        if (!isActive()) {
            return;
        }
        active = false;
        dataModel.mapper(QueueTableSpec.class).update(this, "active");
    }

    @Override
    public void delete() {
        if (isActive()) {
            deactivate();
        }
        dataModel.mapper(QueueTableSpec.class).remove(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMultiConsumer() {
        return multiConsumer;
    }

    @Override
    public boolean isPrioritized() {
        return isPrioritized;
    }

    @Override
    public String getPayloadType() {
        return payloadType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean buffered, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean isPrioritized) {
        DestinationSpec spec = OfflineDestinationSpecImpl.from(dataModel,this, name, retryDelay, retries, buffered, isDefault, queueTypeName, isExtraQueueCreationEnabled, isPrioritized);
        spec.save();
        return spec;
    }

    @Override
    public DestinationSpec createDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled, boolean isPrioritized) {
        return createDestinationSpec(name, retryDelay, retries, false, isDefault, queueTypeName, isExtraQueueCreationEnabled, isPrioritized);
    }

    @Override
    public DestinationSpec createBufferedDestinationSpec(String name, int retryDelay, int retries, boolean isDefault, String queueTypeName, boolean isExtraQueueCreationEnabled) {
        return createDestinationSpec(name, retryDelay, retries, true, isDefault, queueTypeName, isExtraQueueCreationEnabled,false);
    }

    @Override
    public boolean isJms() {
        return false;
    }
    
    @Override
    public void save() {
        if (fromDB) {
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
            fromDB = true;
        }
    }

}
