/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.store.InsightProcessTypeStore', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        valueType: ['deviceId'],
                        displayType: Uni.I18n.translate('imt.object.device', 'IMT', 'Device')
                    },
                    {
                    	valueType: ['usagePointId'],
                        displayType: Uni.I18n.translate('imt.object.usagepoint', 'IMT', 'UsagePoint')
                    },
                    {
                        valueType: ['alarmId'],
                        displayType: Uni.I18n.translate('imt.object.alarm', 'IMT', 'Alarm')
                    },
                    {
                        valueType: ['issueId'],
                        displayType: Uni.I18n.translate('imt.object.issue', 'IMT', 'Issue')
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