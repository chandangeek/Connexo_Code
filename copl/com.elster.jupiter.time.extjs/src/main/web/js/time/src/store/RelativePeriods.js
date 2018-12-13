/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.store.RelativePeriods', {
    extend: 'Ext.data.Store',
    model: 'Tme.model.RelativePeriod',
    autoLoad: false,
    storeId: 'timeUnits',
    proxy: {
        type: 'rest',
        url: '../../api/tmr/relativeperiods',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
