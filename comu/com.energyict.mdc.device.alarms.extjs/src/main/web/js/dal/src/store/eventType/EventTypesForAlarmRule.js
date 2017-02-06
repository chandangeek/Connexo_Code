/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.store.eventType.EventTypesForAlarmRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Dal.model.eventType.EventTypeForAddAlarmRuleGrid',
    storeId: 'EventTypesForTask',

    requires: [
        'Dal.model.eventType.EventTypeForAddAlarmRuleGrid'
    ]
});
