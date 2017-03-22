/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.store.DataQuality', {
    extend: 'Ext.data.Store',
    model: 'Ddv.model.DataQuality',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/ddq/dataQualityResults',
        reader: {
            type: 'json',
            root: 'dataQualityResults',
            totalProperty: 'total'
        }
    }
});
