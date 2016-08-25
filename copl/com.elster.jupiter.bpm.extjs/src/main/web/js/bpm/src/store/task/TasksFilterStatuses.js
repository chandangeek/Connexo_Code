Ext.define('Bpm.store.task.TasksFilterStatuses', {
    extend: 'Ext.data.Store',

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([Ext.apply({
            data: [
                {
                    value: 'CREATED',
                    display: Uni.I18n.translate('bpm.filter.created', 'BPM', 'Created')
                },
                {
                    value: 'ASSIGNED',
                    display: Uni.I18n.translate('bpm.filter.assigned', 'BPM', 'Assigned')
                },
                {
                    value: 'ONGOING',
                    display: Uni.I18n.translate('bpm.filter.ongoingStatus', 'BPM', 'Ongoing')
                },
                {
                    value: 'COMPLETED',
                    display: Uni.I18n.translate('bpm.filter.completed', 'BPM', 'Completed')
                },
                {
                    value: 'FAILED',
                    display: Uni.I18n.translate('bpm.filter.failed', 'BPM', 'Failed')
                },
                {
                    value: 'CANCELLED',
                    display: Uni.I18n.translate('bpm.filter.cancelled', 'BPM', 'Cancelled')
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