/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.store.Purposes', {
    extend: 'Ext.data.Store',
    fields: [
        'id', 'name'
    ],
    proxy: {
        type: 'rest',
        url: '/api/udq/fields/kpiMetrologyPurposes',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'metrologyPurposes'
        }
    }
});
