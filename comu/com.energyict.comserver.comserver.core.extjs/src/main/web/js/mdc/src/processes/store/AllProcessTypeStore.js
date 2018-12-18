/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.store.AllProcessTypeStore', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        value: ['deviceId'],
                        displayType: Uni.I18n.translate('mdc.object.device', 'MDC', 'Device')
                    },
                    {
                        value: ['alarmId'],
                        displayType: Uni.I18n.translate('mdc.object.alarm', 'MDC', 'Alarm')
                    },
                    {
                        value: ['issueId'],
                        displayType: Uni.I18n.translate('mdc.object.issue', 'MDC', 'Issue')
                    }
                ],
                fields: [
                    {
                        name: 'value'
                    },
                    {
                        name: 'displayType'
                    }
                ]
            },
            cfg
        )])
        ;
    }
})
;