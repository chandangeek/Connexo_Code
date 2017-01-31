/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.store.RelativePeriodCategories', {
    extend: 'Ext.data.Store',
    storeId: 'relativePeriodCategories',
    autoLoad: false,
    model: 'Tme.model.Categories',
    proxy: {
        type: 'rest',
        url: '/api/tmr/relativeperiods/categories',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});
