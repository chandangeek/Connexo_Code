Ext.define('Mdc.store.ConnectionMethodsOfDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.ConnectionMethod'
    ],
    model: 'Mdc.model.ConnectionMethod',
    storeId: 'ConnectionMethodsOfDeviceConfiguration',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/connectionmethods',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
