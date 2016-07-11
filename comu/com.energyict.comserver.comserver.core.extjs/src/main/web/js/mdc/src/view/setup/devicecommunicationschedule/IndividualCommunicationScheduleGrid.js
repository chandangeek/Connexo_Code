Ext.define('Mdc.view.setup.devicecommunicationschedule.IndividualCommunicationScheduleGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.individualCommunicationScheduleGrid',
    ui: 'medium',
    cls: 'no-side-padding',
    requires: [
        'Mdc.util.ScheduleToStringConverter'
    ],
    items: [
        {
            xtype: 'grid',
            itemId: 'individualComtaskGrid',
            padding: '0 0 0 0',
            columns: [
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.communicationTask', 'MDC', 'Commmunication task'),
                    flex: 2,
                    dataIndex: 'comTaskInfos',
                    renderer: function(value){
                        var resultArray = [];
                        Ext.Array.each(value,function(comTask){
                            resultArray.push(Ext.String.htmlEncode(comTask.name));
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
                        return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                    }
                },
                {
                    xtype: 'uni-actioncolumn',
                    menu: {
                        plain: true,
                        border: false,
                        shadow: false,
                        items: [
                            {
                                text: Uni.I18n.translate('deviceCommunicationSchedules.changeFrequency', 'MDC', 'Change frequency'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                itemId: 'changeCommunicationSchedule',
                                action: 'changeCommunicationSchedule'

                            },
                            {
                                text: Uni.I18n.translate('deviceCommunicationSchedules.removeFrequency', 'MDC', 'Remove frequency'),
                                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                itemId: 'removeCommunicationSchedule',
                                action: 'removeCommunicationSchedule'

                            }
                        ]
                    }

                }
            ]
        }
    ],
    initComponent: function () {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Mdc.model.DeviceSchedule',
                data: this.individual
            });
        this.callParent();
        this.down('#individualComtaskGrid').reconfigure(store);
    }
})
;

