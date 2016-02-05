Ext.define('Imt.servicecategories.store.ServiceCategories', {
    extend: 'Ext.data.Store',
    model: 'Imt.servicecategories.model.ServiceCategory',
    proxy: {
        type: 'rest',
        url: '/api/mtr/servicecategory',
        reader: {
            type: 'json',
            root: 'categories'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});