/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.store.UpdateTimeframes',  {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.ExportPeriod',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/tmr/relativeperiods',
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined,
        reader: {
            type: 'json',
            root: 'data'
        },
        //params: {
        //    category: 'relativeperiod.category.updateTimeframe'
        //}
    }
});