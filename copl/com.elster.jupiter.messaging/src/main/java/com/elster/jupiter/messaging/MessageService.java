/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * Main service of the MSG module that allows managing messaging.
 */
@ProviderType
public interface MessageService {

    String DESTINATION_NAME = "destination";
    String SUBSCRIBER_NAME = "subscriber";
    String COMPONENTNAME = "MSG";
    String PRIORITIZED_RAW_QUEUE_TABLE = "MSG_PRIO_RAWQUEUETABLE";

    default QueueTableSpec createQueueTableSpec(String name, String payloadType, String storageClause, boolean multiConsumer) {
        return createQueueTableSpec(name, payloadType, storageClause, multiConsumer, false);
    }

    /**
     * Creates a new persisted and activated QueueTableSpec
     *
     * @param name
     * @param payloadType
     * @param multiConsumer
     * @param isPrioritized
     * @return
     */
    QueueTableSpec createQueueTableSpec(String name, String payloadType, String storageClause, boolean multiConsumer, boolean isPrioritized);

    enum QueueTable {
        JUPITEREVENTS_RAW_QUEUE_TABLE("QTAB_JUPITEREVENTS", "PCTFREE 20 PCTUSED 40 INITRANS 4 MAXTRANS 255 STORAGE(INITIAL 8 NEXT 81 MINEXTENTS 1 MAXEXTENTS 2147483645 " +
                "PCTINCREASE 0 FREELISTS 4 FREELIST GROUPS 1)");

        private String queueTableName;
        private String storageClause;

        QueueTable(String queueTableName, String storageClause) {
            this.queueTableName = queueTableName;
            this.storageClause = storageClause;
        }

        public String getQueueTableName() {
            return queueTableName;
        }

        public String getStorageClause() {
            return storageClause;
        }
    }

    /**
     * @param name
     * @return the QueueTableSpec with the given name, optional, as it may not exist.
     */
    Optional<QueueTableSpec> getQueueTableSpec(String name);

    /**
     * @param name
     * @return the DestinationSpec with the given name, optional, as it may not exist
     */
    Optional<DestinationSpec> getDestinationSpec(String name);

    /**
     * @param name
     * @param version
     * @return the DestinationSpec with the given name, optional, as it may not exist
     */
    Optional<DestinationSpec> lockDestinationSpec(String name, long version);

    /**
     * @param destinationSpecName
     * @param name
     * @return the SubscriberSpec with the given name for the Destination with the given name, optional, as it may not exist
     */
    Optional<SubscriberSpec> getSubscriberSpec(String destinationSpecName, String name);

    /**
     * @return a List containing all SubscriberSpecs
     */
    List<SubscriberSpec> getSubscribers();

    List<SubscriberSpec> getNonSystemManagedSubscribers();

    List<DestinationSpec> getDestinationSpecs(String queueTypeName);

    List<DestinationSpec> findDestinationSpecs();
}
