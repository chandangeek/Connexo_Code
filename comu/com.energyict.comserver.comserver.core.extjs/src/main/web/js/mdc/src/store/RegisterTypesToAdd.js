Ext.define('Mdc.store.RegisterTypesToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],

    model: 'Mdc.model.RegisterType',

    buffered: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '/api/dtc/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});