/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.MeterExportGroups', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.MeterGroup'
    ],
    model: 'Mdc.model.MeterGroup',
    storeId: 'MeterExportGroups',
    proxy: {
        type: 'rest',
        url: '/api/ddr/kpis/groups',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'deviceGroups'
        }
    }
});
