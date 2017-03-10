/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceDataEstimationRulesSet', {
    extend: 'Ext.data.Store',
    requires: ['Mdc.model.DeviceDataEstimationRulesSet'],
    model: 'Mdc.model.DeviceDataEstimationRulesSet',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/estimationrulesets',
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'estimationRuleSets',
            totalProperty: 'total'
        }
    }
});