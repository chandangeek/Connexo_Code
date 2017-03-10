/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DeviceConfigValidationRuleSets', {
    extend: 'Ext.data.Store',

    requires: [
        'Cfg.model.ValidationRuleSet'
    ],

    model: 'Cfg.model.ValidationRuleSet',
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