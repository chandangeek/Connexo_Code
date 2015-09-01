Ext.define('Mdc.store.RegisterTypesToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.RegisterTypeForLoadProfileType'
    ],

    model: 'Mdc.model.RegisterTypeForLoadProfileType',

    buffered: true,
    remoteFilter: true,
    pageSize: 200,

    proxy: {
        type: 'rest',
        baseUrl: '/api/mds/loadprofiles/measurementtypes',
        url: '/api/mds/loadprofiles/measurementtypes',
        reader: {
            type: 'json',
            root: 'registerTypes'
        }
    }
});