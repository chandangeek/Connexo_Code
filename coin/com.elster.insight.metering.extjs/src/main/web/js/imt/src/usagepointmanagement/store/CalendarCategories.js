/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.CalendarCategories', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name','displayName'],
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