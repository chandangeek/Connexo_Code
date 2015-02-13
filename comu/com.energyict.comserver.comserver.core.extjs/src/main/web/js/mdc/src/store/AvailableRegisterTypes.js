Ext.define('Mdc.store.AvailableRegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],

    model: 'Mdc.model.RegisterType',
    storeId: 'AvailableRegisterTypes',

    buffered: true,

    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});