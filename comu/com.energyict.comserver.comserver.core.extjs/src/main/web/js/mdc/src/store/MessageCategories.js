Ext.define('Mdc.store.MessageCategories', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MessageCategory'
    ],
    model: 'Mdc.model.MessageCategory',
    storeId: 'MessageCategories',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/devicemessageenablements',
        reader: {
            type: 'json',
            root: 'categories'
        },
        timeout: 300000,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});