/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.store.DataValidationKpis', {
    extend: 'Ext.data.Store',
    requires: [
        'Cfg.model.DataValidationKpi'
    ],
    model: 'Cfg.model.DataValidationKpi',
    storeId: 'DataValidationKpis',
    proxy: {
        type: 'rest',
        url: '/api/dqk/deviceKpis',
        reader: {
            type: 'json',
            root: 'deviceKpis'
        }
    }
});
