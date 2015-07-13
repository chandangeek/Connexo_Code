package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.orm.TransactionRequired;
import com.energyict.mdc.masterdata.MasterDataService;

/**
 * Models the different event types that are produced by the master data bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:51)
 */
public enum EventType {

    LOADPROFILETYPE_CREATED("loadprofiletype/CREATED"),
    LOADPROFILETYPE_UPDATED("loadprofiletype/UPDATED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("oldObisCode", ValueType.STRING, "oldObisCode");
            eventTypeBuilder.withProperty("oldIntervalSeconds", ValueType.LONG, "oldIntervalSeconds");
            return eventTypeBuilder;
        }
    },
    LOADPROFILETYPE_DELETED("loadprofiletype/DELETED"),
    LOADPROFILETYPE_VALIDATEDELETE("loadprofiletype/VALIDATEDELETE"),
    CHANNEL_TYPE_LOADPROFILETYPE_VALIDATEDELETE("channeltypeinloadprofiletype/VALIDATEDELETE") {
        @Override
        void install(EventService eventService) {
            EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                    .name(name())
                    .component(MasterDataService.COMPONENTNAME)
                    .category("Crud")
                    .scope("System");
            this.addCustomProperties(builder).create().save();
        }
    },
    MEASUREMENTTYPE_CREATED("measurementtype/CREATED"),
    MEASUREMENTTYPE_UPDATED("measurementtype/UPDATED") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("oldObisCode", ValueType.STRING, "oldObisCode");
            return eventTypeBuilder;
        }
    },
    MEASUREMENTTYPE_DELETED("measurementtype/DELETED"),
    MEASUREMENTTYPE_VALIDATEDELETE("measurementtype/VALIDATEDELETE"),
    REGISTERGROUP_CREATED("registergroup/CREATED"),
    REGISTERGROUP_UPDATED("registergroup/UPDATED"),
    REGISTERGROUP_DELETED("registergroup/DELETED"),
    REGISTERGROUP_VALIDATEDELETE("registergroup/VALIDATEDELETE"),
    LOGBOOKTYPE_CREATED("logbooktype/CREATED"),
    LOGBOOKTYPE_UPDATED("logbooktype/UPDATED"),
    LOGBOOKTYPE_VALIDATEDELETE("logbooktype/VALIDATEDELETE") {
        @Override
        protected EventTypeBuilder addCustomProperties(EventTypeBuilder etb) {
            EventTypeBuilder eventTypeBuilder = super.addCustomProperties(etb);
            eventTypeBuilder.withProperty("oldObisCode", ValueType.STRING, "oldObisCode");
            return eventTypeBuilder;
        }
    },
    LOGBOOKTYPE_DELETED("logbooktype/DELETED");

    private static final String NAMESPACE = "com/energyict/mdc/masterdata/";
    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return NAMESPACE + topic;
    }

    @TransactionRequired
    void install(EventService eventService) {
        EventTypeBuilder builder = eventService.buildEventTypeWithTopic(topic())
                .name(name())
                .component(MasterDataService.COMPONENTNAME)
                .category("Crud")
                .scope("System")
                .withProperty("id", ValueType.LONG, "id");
        this.addCustomProperties(builder).create().save();
    }

    protected EventTypeBuilder addCustomProperties(EventTypeBuilder eventTypeBuilder) {
        return eventTypeBuilder;
    }

}