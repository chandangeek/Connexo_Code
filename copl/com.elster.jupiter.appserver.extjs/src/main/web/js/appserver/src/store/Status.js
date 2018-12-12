/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.store.Status', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'FAILED',
                    display: Uni.I18n.translate('customTask.history.failStatus', 'APR', 'Failed')
                },
                {
                    value: 'BUSY',
                    display: Uni.I18n.translate('customTask.history.busyStatus', 'APR', 'Ongoing')
                },
                {
                    value: 'SUCCESS',
                    display: Uni.I18n.translate('customTask.history.successStatus', 'APR', 'Successful')
                }
            ],
            fields: [
                {
                    name: 'value'
                },
                {
                    name: 'display'
                }
            ]
        }, cfg)]);
    }
});
