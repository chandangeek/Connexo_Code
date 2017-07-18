/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.store.GasDayYearStart', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.GasDayYearStart',

    proxy: {
        type: 'rest',
        url: '/api/mtr/gasday/yearstart',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});