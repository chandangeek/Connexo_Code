/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RuleDeviceConfigurationsNotLinked', {
    extend: 'Ext.data.Store',
    model: 'Mdc.model.RuleDeviceConfiguration',
    buffered: true,
    storeId: 'RuleDeviceConfigurationsNotLinked',
    pageSize: 10,
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/validationruleset/{ruleSetId}/linkabledeviceconfigurations',
        timeout: 240000,
        reader: {
            type: 'json',
            totalProperty: 'total',
            root: 'deviceConfigurations'
        }
    }
});

