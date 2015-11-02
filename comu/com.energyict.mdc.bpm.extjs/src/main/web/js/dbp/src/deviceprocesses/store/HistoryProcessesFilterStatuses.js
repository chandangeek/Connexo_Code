Ext.define('Dbp.deviceprocesses.store.HistoryProcessesFilterStatuses', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
                data: [
                    {
                        value: 2,
                        display: Uni.I18n.translate('dbp.status.completed', 'DBP', 'Completed')
                    },
                    {
                        value: 3,
                        display: Uni.I18n.translate('dbp.status.aborted', 'DBP', 'Aborted')
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