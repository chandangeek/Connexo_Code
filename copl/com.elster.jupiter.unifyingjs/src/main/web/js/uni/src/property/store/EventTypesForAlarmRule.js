/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.store.EventTypesForAlarmRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Uni.property.model.EventTypeForAddAlarmRuleGrid',
    storeId: 'EventTypesForTask',

    requires: [
        'Uni.property.model.EventTypeForAddAlarmRuleGrid'
    ]
});