Ext.define('Mdc.store.RegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'RegisterTypes',
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});
