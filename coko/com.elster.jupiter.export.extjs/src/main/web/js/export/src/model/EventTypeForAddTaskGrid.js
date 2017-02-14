/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.EventTypeForAddTaskGrid', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'eventFilterCode'
        },
        // The next 4 fields are used for the tooltip:
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
        }
    ]
});
