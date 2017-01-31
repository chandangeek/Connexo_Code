/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.ServiceCategories', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.ServiceCategory',
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/servicecategory',
        reader: {
            type: 'json',
            root: 'categories'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});