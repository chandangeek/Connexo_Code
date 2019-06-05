/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConfigValidationRuleSets', {
    extend: 'Ext.data.Store',

    requires: [
        'Mdc.model.DeviceConfigValidationRuleSet'
    ],

    model: 'Mdc.model.DeviceConfigValidationRuleSet',
    storeId: 'DeviceConfigValidationRuleSets',

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/validationrulesets',
        reader: {
            type: 'json',
            root: 'validationRuleSets',
            totalProperty: 'total'
        }
    }
});