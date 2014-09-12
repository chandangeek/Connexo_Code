Ext.define('Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskGrid',
    overflowY: 'auto',
    itemId: 'deviceCommunicationTaskGrid',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ConnectionMethodsOfDevice',
        'Uni.grid.column.Default',
        'Uni.grid.column.Default',
        'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskActionMenu'
    ],

    store: 'CommunicationTasksOfDevice',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceCommunicationTask.communicationTask', 'MDC', 'Communication task'),
                dataIndex: 'comTask',
                flex: 1,
                renderer: function (value) {
                        return value.name;
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.frequency', 'MDC', 'Frequency'),
                dataIndex: 'temporalExpression',
                flex: 1,
                renderer: function(value){
                    return Mdc.util.ScheduleToStringConverter.convert(value);
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.scheduleName', 'MDC', 'Schedule name'),
                dataIndex: 'scheduleName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.nextCommunication', 'MDC', 'Next communication'),
                dataIndex: 'nextCommunication',
                flex: 2,
                renderer: function (value) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    } else {
                        return '';
                    }
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.connectionMethod', 'MDC', 'Connection method'),
                dataIndex: 'connectionMethod',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.devicecommunicationtask.DeviceCommunicationTaskActionMenu'
            }

        ];

        me.callParent();
    }
});



