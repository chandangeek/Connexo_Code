/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.model.UsagePoint', {
    extend: 'Uni.model.Version',
    requires: ['Mdc.usagepointmanagement.model.MeterActivations'],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'mRID', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'serviceCategory', type: 'string', defaultValue: null, useNull: true},
        {name: 'version', type: 'number', useNull: true},
        {name: 'installationTime', type: 'int', defaultValue: null, useNull: true},
        {name: 'metrologyConfigurationVersion', type: 'auto', defaultValue: null, useNull: true},
        {name: 'meterActivations', type: 'auto', defaultValue: null, useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '/api/mtr/usagepoints/',
        timeout: 240000,
        reader: {
            type: 'json'
        }
    }
});
