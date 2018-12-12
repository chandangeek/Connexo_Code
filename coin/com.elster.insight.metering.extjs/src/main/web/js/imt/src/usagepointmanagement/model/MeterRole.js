/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.model.MeterRole', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        {
            name: 'name',
            mapping: 'meterRole.name'
        },
        {
            name: 'required',
            mapping: 'meterRole.required'
        },
        {
            name: 'activationTime',
            mapping: 'meterRole.activationTime'
        },
        {
            name: 'meter',
            mapping: 'meter.name'
        },
        {
            name: 'url',
            mapping: 'meter.url'
        }
    ]
});