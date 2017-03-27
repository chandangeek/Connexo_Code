/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.store.DataQuality', {
    extend: 'Ext.data.Store',
    model: 'Imt.dataquality.model.DataQuality',
    pageSize: 10,

    proxy: {
        type: 'rest',
        url: '/api/udq/dataQualityResults',
        reader: {
            type: 'json',
            root: 'dataQualityResults',
            totalProperty: 'total'
        }
    }
});
