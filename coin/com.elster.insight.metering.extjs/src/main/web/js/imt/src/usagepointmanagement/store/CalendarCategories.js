Ext.define('Imt.usagepointmanagement.store.CalendarCategories', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    proxy: {
        type: 'rest',
        url: '/api/cal/categories/usedcategories',
        reader: {
            type: 'json',
            root: 'categories'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});