/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.UsagePointHistoryDevice', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'serialNumber', type: 'string'},
        {name: 'state', type: 'string'},
        {name: 'start', type: 'date', dateFormat: 'time'},
        {name: 'end', type: 'date', dateFormat: 'time'},
        {name: 'active', type: 'boolean'},
        {name: 'deviceType', type: 'auto'}
    ]
});
