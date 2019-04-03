/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ValidationRuleSetsForDeviceConfig', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.DeviceConfigValidationRuleSet',
    buffered: true,
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/linkablevalidationrulesets',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'validationRuleSets',
            totalProperty: 'total'
        }
    }
});
