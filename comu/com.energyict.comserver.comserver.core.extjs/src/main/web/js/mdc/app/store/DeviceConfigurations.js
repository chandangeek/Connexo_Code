Ext.define('Mdc.store.DeviceConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceConfiguration'
    ],
    model: 'Mdc.model.DeviceConfiguration',
    storeId: 'DeviceConfigurations',
    pageSize: 10,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations',
        reader: {
            type: 'json',
            root: 'deviceConfigurations'
        }
    }
});