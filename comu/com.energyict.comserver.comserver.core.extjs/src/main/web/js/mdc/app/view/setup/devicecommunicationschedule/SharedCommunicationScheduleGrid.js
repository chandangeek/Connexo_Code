Ext.define('Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.sharedCommunicationScheduleGrid',
    ui: 'medium',
    requires: [
        'Mdc.util.ScheduleToStringConverter',
        'Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleActionMenu',
        'Uni.'
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
                    renderer: function(value){
                        var resultArray = [];
                        Ext.Array.each(value,function(comTask){
                            resultArray.push('<li>'+comTask.name+'</li>');
                        });
                        return resultArray.join('<br>');
                    }
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.schedule', 'MDC', 'Schedule'),
                    flex: 1,
                    dataIndex: 'schedule',
                    renderer: function(value){
                        return Mdc.util.ScheduleToStringConverter.convert(value);
                    }
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.plannedDate', 'MDC', 'Planned date'),
                    flex: 1,
                    dataIndex: 'plannedDate',
                    renderer: function (value) {
                        if (value !== null) {
                            return new Date(value).toLocaleString();
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
        var me = this;
          var store = Ext.create('Ext.data.Store',{
                    model: 'Mdc.model.DeviceSchedule',
                    data: this.shared
            });
        this.callParent();
        this.down('#sharedCommunicationScheduleGrid').reconfigure(store);
    }
})
;

