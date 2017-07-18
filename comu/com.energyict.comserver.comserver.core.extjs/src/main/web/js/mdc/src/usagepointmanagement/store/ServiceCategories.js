/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.store.ServiceCategories', {
    extend: 'Ext.data.Store',
    model: 'Mdc.usagepointmanagement.model.ServiceCategory',
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