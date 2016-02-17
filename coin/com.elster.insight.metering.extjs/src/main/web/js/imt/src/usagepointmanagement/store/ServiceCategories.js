Ext.define('Imt.usagepointmanagement.store.ServiceCategories', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.ServiceCategory',
    proxy: {
        type: 'rest',
        url: '/api/up/servicecategory',
        reader: {
            type: 'json',
            root: 'categories'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});