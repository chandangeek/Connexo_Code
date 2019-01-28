/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.AllProcessesStatusStore', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        value: 1,
                        display: Uni.I18n.translate('mdc.processstatus.active', 'MDC', 'Active')
                    },
                    {
                        value: 2,
                        display: Uni.I18n.translate('mdc.processstatus.completed', 'MDC', 'Completed')
                    },
                    {
                        value: 3,
                        display: Uni.I18n.translate('mdc.processstatus.cancelled', 'MDC', 'Cancelled')
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
            },
            cfg
        )])
        ;
    }
})
;