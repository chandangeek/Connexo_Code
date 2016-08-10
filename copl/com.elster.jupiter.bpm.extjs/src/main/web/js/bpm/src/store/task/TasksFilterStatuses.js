Ext.define('Bpm.store.task.TasksFilterStatuses', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'OPEN',
                    display: Uni.I18n.translate('bpm.filter.openStatus', 'BPM', 'Open')
                },
                {
                    value: 'INPROGRESS',
                    display: Uni.I18n.translate('bpm.filter.ongoingStatus', 'BPM', 'Ongoing')
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