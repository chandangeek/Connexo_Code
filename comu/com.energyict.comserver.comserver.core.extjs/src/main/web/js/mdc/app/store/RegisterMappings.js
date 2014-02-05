Ext.define('Mdc.store.RegisterMappings', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterMapping'
    ],
    model: 'Mdc.model.RegisterMapping',
    storeId: 'RegisterMappings',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registers',
        reader: {
            type: 'json',
            root: 'registers'
        }
    }
});
