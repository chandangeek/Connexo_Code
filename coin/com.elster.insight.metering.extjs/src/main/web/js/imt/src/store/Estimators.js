/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.store.Estimators', {
    extend: 'Ext.data.Store',
    requires: [
        'Imt.model.Estimator'
    ],
    model: 'Imt.model.Estimator',
    proxy: {
        type: 'rest',
        url: '/api/est/estimation/estimators',
        reader: {
            type: 'json',
            root: 'estimators',
            totalProperty: 'total'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
