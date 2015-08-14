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
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceCommunicationTask.pagingtoolbartop.displayMsg', 'MDC', '{2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('deviceCommunicationTask.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('deviceCommunicationTask.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communication tasks to display')
            }
        ];
        me.columns = [
            {
                header: Uni.I18n.translate('deviceCommunicationTask.communicationTask', 'MDC', 'Communication task'),
                dataIndex: 'comTask',
                flex: 1,
                renderer: function (value) {
                        return Ext.String.htmlEncode(value.name);
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
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
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
                        return Uni.DateTime.formatDateTimeLong(new Date(value));
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
                renderer: function(value,metaData,rowValue) {
                    if(!rowValue.data.connectionDefinedOnDevice){
                         metaData.tdAttr = 'data-qtip="' + Uni.I18n.translate('deviceCommunicationTask.connectionNotDefinedOnDevice', 'MDC', 'This connection method is not defined on the device yet') + '"';
                         return '<tpl><img src="../sky/build/resources/images/shared/bullet-red.png" class="ct-result ct-failure"><span style="position: relative; top: -3px; left: 4px">' + Ext.String.htmlEncode(value) + '</span></tpl>'
                    } else {
                        return Ext.String.htmlEncode(value);
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



