/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fim.store.Status', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'FAILURE',
                    display: Uni.I18n.translate('importService.history.failStatus', 'FIM', 'Failed')
                },
                {
                    value: 'PROCESSING',
                    display: Uni.I18n.translate('importService.history.busyStatus', 'FIM', 'Ongoing')
                },
                {
                    value: 'SUCCESS',
                    display: Uni.I18n.translate('importService.history.successStatus', 'FIM', 'Successful')
                },
                {
                    value: 'SUCCESS_WITH_FAILURES',
                    display: Uni.I18n.translate('importService.history.successWithErrorsStatus', 'FIM', 'Partial success')
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