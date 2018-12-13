/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.model.EventTypeForAddAlarmRuleGrid', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'eventFilterCode'
        },
        {
            name: 'eventFilterCodeUnformatted'
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
