/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RuleDeviceConfigurations', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.RuleDeviceConfiguration',
    pageSize: 10,
    proxy: {
        type: 'rest',
        timeout: 120000,
        url: '/api/dtc/validationruleset/{ruleSetId}/deviceconfigurations',
        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'deviceConfigurations'
        }
    }
});
