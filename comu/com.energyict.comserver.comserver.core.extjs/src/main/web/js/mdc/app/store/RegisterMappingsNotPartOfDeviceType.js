Ext.define('Mdc.store.RegisterMappingsNotPartOfDeviceType', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterMapping'
    ],
    model: 'Mdc.model.RegisterMapping',
    storeId: 'RegisterMappings',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registersNotPartOfDeviceType?available=true',
        reader: {
            type: 'json',
            root: 'registers'
        }
    }
});