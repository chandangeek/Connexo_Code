Ext.define('Mdc.store.RegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'RegisterTypes',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/mdc/registertypes',
        reader: {
            type: 'json',
            root: 'RegisterType'
        }
    }
});