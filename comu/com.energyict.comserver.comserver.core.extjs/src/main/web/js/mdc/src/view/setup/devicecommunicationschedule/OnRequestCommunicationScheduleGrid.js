Ext.define('Mdc.view.setup.devicecommunicationschedule.OnRequestCommunicationScheduleGrid', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.onRequestCommunicationScheduleGrid',
    ui: 'medium',
    cls: 'no-side-padding',
    requires: [
        'Mdc.util.ScheduleToStringConverter'
    ],
    items: [
        {
            xtype: 'grid',
            itemId: 'onRequestComtaskGrid',
            padding: '0 0 0 0',
            columns: [
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.communicationTask', 'MDC', 'Commmunication task'),
                    flex: 3,
                    dataIndex: 'comTaskInfos',
                    renderer: function (value) {
                        var resultArray = [];
                        Ext.Array.each(value, function (comTask) {
                            resultArray.push(comTask.name);
                        });
                        return resultArray.join('<br>');
                    }
                },
                {
                    header: Uni.I18n.translate('deviceCommunicationSchedules.nextCommunication', 'MDC', 'Next communication'),
                    flex: 1,
                    dataIndex: 'nextCommunication',
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
                                text: Uni.I18n.translate('deviceCommunicationSchedules.addFrequency', 'MDC', 'Add frequency'),
                                itemId: 'addCommunicationSchedule',
                                action: 'addCommunicationSchedule'

                            },
                            {
                                text: Uni.I18n.translate('deviceCommunicationSchedules.run', 'MDC', 'Run'),
                                itemId: 'runCommunicationSchedule',
                                action: 'runCommunicationSchedule'

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
        var store = Ext.create('Ext.data.Store', {
            model: 'Mdc.model.DeviceSchedule',
            data: this.onRequest
        });
        this.callParent();
        this.down('#onRequestComtaskGrid').reconfigure(store);
    }
})
;

