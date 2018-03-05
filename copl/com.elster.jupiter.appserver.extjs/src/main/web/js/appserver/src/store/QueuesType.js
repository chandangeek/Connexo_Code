/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.QueuesType', {
    extend: 'Ext.data.Store',
    model: 'Apr.model.QueueType',
    proxy: {
        type: 'memory'
    },
    data: [
        {
            name: Uni.I18n.translate('genearal.messagequeues.DataExport', 'APR', 'Data Export'),
            value: 'DataExport'
        },
        {
            name: Uni.I18n.translate('genearal.messagequeues.DataEstimation', 'APR', 'Data Estimation'),
            value: 'DataEstimation'
        },
        {
            name: Uni.I18n.translate('genearal.messagequeues.DataValidation', 'APR', 'Data Validation'),
            value: 'DataValidation'
        }
    ]
});