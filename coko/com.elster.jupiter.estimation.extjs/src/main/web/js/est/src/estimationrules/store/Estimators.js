/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.store.Estimators', {
    extend: 'Ext.data.Store',
    model: 'Est.estimationrules.model.Estimator',
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