/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.DataCollectionKpis', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DataCollectionKpi'
    ],
    model: 'Mdc.model.DataCollectionKpi',
    storeId: 'DataCollectionKpis',
    proxy: {
        type: 'rest',
        url: '/api/ddr/kpis',
        reader: {
            type: 'json',
            root: 'kpis'
        }
    }
});
