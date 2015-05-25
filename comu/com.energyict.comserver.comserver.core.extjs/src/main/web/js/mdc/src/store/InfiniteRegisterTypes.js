Ext.define('Mdc.store.InfiniteRegisterTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterType'
    ],
    model: 'Mdc.model.RegisterType',
    storeId: 'InfiniteRegisterTypes',

    buffered: true,
    remoteFilter: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        url: '../../api/mds/registertypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});
