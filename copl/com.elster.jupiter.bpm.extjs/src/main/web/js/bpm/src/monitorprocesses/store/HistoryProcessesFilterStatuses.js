/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.store.HistoryProcessesFilterStatuses', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        value: 2,
                        display: Uni.I18n.translate('bpm.status.completed', 'BPM', 'Completed')
                    },
                    {
                        value: 3,
                        display: Uni.I18n.translate('bpm.status.aborted', 'BPM', 'Aborted')
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