/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.model.eventType.EventTypeForAddAlarmRuleGrid', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'eventFilterCode'
        },
        // The next 5 fields are used for the tooltip:
        {
            name: 'deviceTypeName',
            persist: false
        },
        {
            name: 'deviceDomainName',
            persist: false
        },
        {
            name: 'deviceSubDomainName',
            persist: false
        },
        {
            name: 'deviceEventOrActionName',
            persist: false
        },
        {
            name: 'deviceCode',
            persist: false
        }
    ]
});
