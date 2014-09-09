Ext.define('Mdc.store.LoadProfileTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    model: 'Mdc.model.LoadProfileType',
    storeId: 'LoadProfileTypes',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/mds/loadprofiles',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});