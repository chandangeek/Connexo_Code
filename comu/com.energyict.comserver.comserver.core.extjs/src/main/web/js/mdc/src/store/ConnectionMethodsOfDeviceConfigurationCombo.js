/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.ConnectionMethodsOfDeviceConfigurationCombo', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ConnectionMethod'
    ],
    model: 'Mdc.model.ConnectionMethod',
    autoLoad: false,
    storeId: 'ConnectionMethodsOfDeviceConfigurationCombo',
    proxy: {
        type: 'rest',
        urlTpl: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
        pageParam: false,
        limitParam: false,
        startParam: false,
        reader: {
            type: 'json',
            root: 'data'
        },

        setUrl: function(deviceType, deviceConfig) {
          this.url = this.urlTpl.replace('{deviceType}', deviceType).replace('{deviceConfig}', deviceConfig);
        }
    }
});
