/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ChannelConfigValidationRules', {
    extend: 'Ext.data.Store',

    requires: [
        'Cfg.model.ValidationRule'
    ],

    model: 'Cfg.model.ValidationRule',
    autoLoad: false,
    storeId: 'ChannelConfigValidationRules',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/channels/{channelConfig}/validationrules',

        reader: {
            type: 'json',
            root: 'validationRules',
            totalProperty: 'total'
        }
    }
});
