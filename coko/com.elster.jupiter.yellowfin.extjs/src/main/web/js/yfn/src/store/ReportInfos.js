/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Yfn.store.ReportInfos
 */
Ext.define('Yfn.store.ReportInfos', {
    extend: 'Ext.data.Store',
    model: 'Yfn.model.ReportInfo',
    storeId: 'ReportInfos',
    autoLoad: false,
    requires:[
       'Yfn.model.ReportInfo'
    ],

    proxy: {
        type: 'ajax',
        url: '/api/yfn/report/info',
        reader: {
            type: 'json',
            root: 'reports'
        }
    }
});