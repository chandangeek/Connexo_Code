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
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
        pageParam: false,
        limitParam: false,
        startParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
