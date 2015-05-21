Ext.define('Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.sharedCommunicationScheduleGrid',
    ui: 'medium',
    cls: 'no-side-padding',

    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleActionMenu'
    ],

    items: [
        {
            xtype: 'grid',
            itemId: 'sharedCommunicationScheduleGrid',
            padding: '0 0 0 0',
            columns: [
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.name', 'MDC', 'Name'),
                    flex: 1,
                    dataIndex: 'name'
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.communicationTasks', 'MDC', 'Commmunication tasks'),
                    flex: 1,
                    dataIndex: 'comTaskInfos',
                    renderer: function (value) {
                        var resultArray = [];
                        Ext.Array.each(value, function (comTask) {
                            resultArray.push('<li>' + Ext.String.htmlEncode(comTask.name) + '</li>');
                        });
                        return resultArray.join('<br>');
                    }
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.frequency', 'MDC', 'Frequency'),
                    flex: 1,
                    dataIndex: 'schedule',
                    renderer: function (value, metadata) {
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
                        return value.every.timeUnit;
                    }
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.plannedDate', 'MDC', 'Planned date'),
                    flex: 1,
                    dataIndex: 'plannedDate',
                    renderer: function (value) {
                        if (value !== null) {
                            return Uni.DateTime.formatDateTimeShort(new Date(value));
                        } else {
                            return '';
                        }
                    }
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleActionMenu'

                }
            ]
        }
    ],

    initComponent: function () {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Mdc.model.DeviceSchedule',
                data: me.shared
            });

        me.callParent();
        me.down('#sharedCommunicationScheduleGrid').reconfigure(store);
    }
});