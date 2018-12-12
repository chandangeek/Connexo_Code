/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterTypesOfDevicetype', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterTypeOnDeviceType'
    ],
    model: 'Mdc.model.RegisterTypeOnDeviceType',
    storeId: 'RegisterTypesOfDevicetype',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});
