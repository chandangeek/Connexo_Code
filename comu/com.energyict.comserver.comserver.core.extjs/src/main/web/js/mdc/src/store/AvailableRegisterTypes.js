/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableRegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],

    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypes',

    pageSize: 200,
    buffered: true,

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});