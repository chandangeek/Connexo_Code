Ext.define('Mdc.store.DeviceConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    model: 'Mdc.model.DeviceConfiguration',
    storeId: 'DeviceConfigurations',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        baseUrl: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        }
    }
});