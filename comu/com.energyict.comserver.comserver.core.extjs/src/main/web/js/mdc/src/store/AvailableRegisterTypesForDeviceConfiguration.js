Ext.define('Mdc.store.AvailableRegisterTypesForDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
//    remoteFilter: true,
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypesForDeviceConfiguration',
   // buffered: true,
   // leadingBufferZone: 50,
   // pageSize: 20,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});