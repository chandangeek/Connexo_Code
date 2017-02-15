/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.store.DataQualityKpis', {
    extend: 'Ext.data.Store',
    model: 'Cfg.insight.dataqualitykpi.model.DataQualityKpi',
    proxy: {
        type: 'rest',
        url: '/api/val/kpis',
        reader: {
            type: 'json',
            root: 'kpis'
        }
    }
});