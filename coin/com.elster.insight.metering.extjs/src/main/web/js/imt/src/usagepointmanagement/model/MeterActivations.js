/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.MeterActivations', {
    extend: 'Ext.data.Model',
    requires: [],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'start', type: 'number', useNull: true},
        {name: 'end', type: 'number', useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'meter', type: 'auto'},
        {name: 'meterRole', type: 'auto'}
    ]
});