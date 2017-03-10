/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.technicalinfo.Thermal', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'pressure', type: 'auto', defaultValue: null},
        {name: 'physicalCapacity', type: 'auto', defaultValue: null},
        {name: 'bypass', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'bypassStatus', type: 'auto', defaultValue: null},
        {name: 'valve', type: 'string', useNull: true, defaultValue: 'UNKNOWN'},
        {name: 'collar', type: 'string', useNull: true, defaultValue: 'UNKNOWN'}
    ]
});