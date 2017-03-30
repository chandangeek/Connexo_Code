/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.AvailableRegisterTypesForDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypesForDeviceConfiguration',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes',
        //url: 'http://localhost:3000/registerTypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});