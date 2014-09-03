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
                            resultArray.push(comTask.name);
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
                    menu: {
                        plain: true,
                        border: false,
                        shadow: false,
                        items: [
                            {
                                text: Uni.I18n.translate('deviceCommunicationSchedules.changeCommunicationSchedule', 'MDC', 'Change communication schedule'),
                                itemId: 'changeCommunicationSchedule',
                                action: 'changeCommunicationSchedule'

                            },
                            {
                                text: Uni.I18n.translate('deviceCommunicationSchedules.removeCommunicationSchedule', 'MDC', 'Remove communication schedule'),
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
        var me = this;
//        this.columns = [
//            {
//                header: Uni.I18n.translate('deviceCommunicationSchedules.name', 'MDC', 'Name'),
//                dataIndex: 'name',
//                flex: 3
//            },
//            {
//                header: Uni.I18n.translate('deviceCommunicationSchedules.status', 'MDC', 'Version'),
//                dataIndex: 'deviceProtocolVersion',
//                flex: 2
//            },
//
//            {
//                xtype: 'uni-actioncolumn'
////                items:'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu'
//            }

//        ];
          var store = Ext.create('Ext.data.Store',{
                    model: 'Mdc.model.DeviceSchedule',
                    data: this.individual
            });
        this.callParent();
        this.down('#individualComtaskGrid').reconfigure(store);
    }
})
;

