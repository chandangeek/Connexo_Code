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
                renderer: function (value, metadata) {
                    if(value) {
                        metadata.tdAttr = 'data-qtip="' + Mdc.util.ScheduleToStringConverter.convert(value) + '"';
                        switch (value.every.timeUnit) {
                            case 'months':
                                return Uni.I18n.translate('general.monthly', 'MDC', 'Monthly');
                            case 'weeks':
                                return Uni.I18n.translate('general.weekly', 'MDC', 'Weekly');
                            case 'days':
                                return Uni.I18n.translate('general.daily', 'MDC', 'Daily');
                            case 'hours':
                                return Uni.I18n.translate('general.hourly', 'MDC', 'Hourly');
                            case 'minutes':
                                return Uni.I18n.translate('general.everyFewMinutes', 'MDC', 'Every few minutes');
                        }
                    } else {
                        metadata.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.noFrequency', 'MDC', 'No frequency') + '"';
                        return '-';
                    }
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.scheduleName', 'MDC', 'Schedule name'),
                dataIndex: 'scheduleName',
                flex: 1,
                renderer: function(value,metadata){
                    if(!value){
                        metadata.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.noSchedule', 'MDC', 'No schedule') + '"';
                        return '-';
                    } else {
                        return value;
                    }
            }

            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function(value,metadata) {
                    if (!value) {
                        metadata.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.noStatus', 'MDC', 'No status') + '"';
                        return '-';
                    } else {
                        return value;
                    }
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.nextCommunication', 'MDC', 'Next communication'),
                dataIndex: 'nextCommunication',
                flex: 2,
                renderer: function (value,metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    } else {
                        metadata.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.noNextCommunication', 'MDC', 'No next communication') + '"';
                        return '-';
                    }
                }
            },
            {
                header: Uni.I18n.translate('deviceCommunicationTask.connectionMethod', 'MDC', 'Connection method'),
                dataIndex: 'connectionMethod',
                flex: 2,
                renderer: function(value,metadata) {
                    if (!value) {
                        metadata.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.noConnectionMethod', 'MDC', 'No connection method') + '"';
                        return '-';
                    } else {
                        return value;
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'device-communication-task-action-menu'
                }
            }

        ];

        me.callParent();
    }
});



