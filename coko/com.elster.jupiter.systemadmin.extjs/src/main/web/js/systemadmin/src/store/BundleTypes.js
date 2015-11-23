Ext.define('Sam.store.BundleTypes', {
    extend: 'Ext.data.Store',
    model: 'Sam.model.BundleType',

    proxy: {
        type: 'rest',
        url: '/api/sys/fields/bundleTypes',
        reader: {
            type: 'json',
            root: 'bundleTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});