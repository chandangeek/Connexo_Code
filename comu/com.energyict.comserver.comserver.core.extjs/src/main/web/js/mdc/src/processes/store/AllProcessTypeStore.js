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
                        valueType: ['deviceId'],
                        displayType: Uni.I18n.translate('mdc.object.device', 'MDC', 'Device')
                    },
                    {
                        valueType: ['alarmId'],
                        displayType: Uni.I18n.translate('mdc.object.alarm', 'MDC', 'Alarm')
                    },
                    {
                        valueType: ['issueId'],
                        displayType: Uni.I18n.translate('mdc.object.issue', 'MDC', 'Issue')
                    }
                ],
                fields: [
                    {
                        name: 'valueType'
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