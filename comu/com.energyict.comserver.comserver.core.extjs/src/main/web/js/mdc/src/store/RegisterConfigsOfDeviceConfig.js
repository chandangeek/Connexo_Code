/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.RegisterConfigsOfDeviceConfig', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterConfiguration'
    ],
    model: 'Mdc.model.RegisterConfiguration',
    storeId: 'RegisterConfigsOfDeviceConfig',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/registerconfigurations',
        reader: {
            type: 'json',
            root: 'registerConfigurations'
        }
    }
});
