Ext.define('Mdc.store.AvailableRegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypes',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes?available=true',
        reader: {
            type: 'json',
            root: 'registers'
        }
    }
});