Ext.define('Imt.usagepointmanagement.store.Purposes', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.Purpose',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{mRID}/purposes',
        reader: {
            type: 'json',
            root: 'purposes'
        },
        remoteFilter: false,
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});