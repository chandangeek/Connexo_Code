/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterConfigValidationRules', {
    extend: 'Ext.data.Store',

    requires: [
        'Cfg.model.ValidationRule'
    ],

    model: 'Cfg.model.ValidationRule',
    autoLoad: false,
    storeId: 'RegisterConfigValidationRules',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registers/{registerConfig}/validationrules',

        //url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registers/{registerConfig}/validationrules',
        reader: {
            type: 'json',
            root: 'validationRules',
            totalProperty: 'total'
        }
    }
});
