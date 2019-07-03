/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.store.InsightProcessesStatusStore', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        value: 1,
                        display: Uni.I18n.translate('imt.processstatus.active', 'IMT', 'Active')
                    },
                    {
                        value: 2,
                        display: Uni.I18n.translate('imt.processstatus.completed', 'IMT', 'Completed')
                    },
                    {
                        value: 3,
                        display: Uni.I18n.translate('imt.processstatus.cancelled', 'IMT', 'Cancelled')
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