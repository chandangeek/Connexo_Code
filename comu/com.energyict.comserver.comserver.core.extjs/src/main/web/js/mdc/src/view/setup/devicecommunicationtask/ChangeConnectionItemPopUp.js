Ext.define('Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp', {
    extend: 'Ext.window.Window',
    alias: 'widget.changeConnectionItemPopUp',
    requires: [
        'Mdc.widget.ScheduleField'
    ],
    closable: false,
    autoShow: true,
    modal: true,
    floating: true,
    border: true,
    itemId: 'changeConnectionItemPopUp',
    shadow: true,
    items: [
        {
            xtype: 'component',
            itemId: 'sharedScheduleWarning',
            html: ''
        },
        {
            xtype: 'container',
            margin: '10px',
            items: [
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'changeConnectionItemForm',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: '&nbsp',
                    labelWidth: 250,
                    layout: {
                        type: 'hbox'
                        //     align: 'stretch'
                    },
                    width: '100%',
                    items: [
                        {
                            xtype: 'component',
                            flex: 1
                        },
                        {
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            ui: 'action',
                            //action: 'addIndividualScheduleAction',
                            itemId: 'changeButton',
                            flex: 0
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            handler: function (button) {
                                button.up('.window').close();
                            },
                            flex: 0
                        }
                    ]
                }
            ]
        }

    ],
    initComponent: function () {
        var me = this;
        this.callParent(arguments);
        this.down('#changeButton').action = this.action;
        if(this.scheduleName){
            this.down('#sharedScheduleWarning').html =
                Ext.String.format(Uni.I18n.translate('deviceCommunicationTask.sharedScheduleWarning.part1', 'MDC', "'{0}' is part of the shared communication schedule '{1}'."),this.comTaskName,this.scheduleName) +
                '<BR>' + Uni.I18n.translate('deviceCommunicationTask.sharedScheduleWarning.part2', 'MDC', 'Changes will be applied to all other communication tasks in this schedule.');
        }
        switch (this.action) {
            case 'changeConnectionMethodOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethod', 'MDC', 'Change connection method'));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethod', 'MDC', 'Connection method'),
                        name: 'name',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init
                    });
                break;
            }
            case 'changeFrequencyOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeFrequencyOfDeviceComTask', 'MDC', 'Change frequency'));
                this.down('#changeConnectionItemForm').add({
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    msgTarget: 'under',
                    required: true,
                    items: [
                        {
                            xtype: 'scheduleField',
                            name: 'schedule',
                            itemId: 'scheduleField',
                            hourCfg: {
                                width: 60
                            },
                            minuteCfg: {
                                width: 60
                            },
                            secondCfg: {
                                width: 60
                            },
                            unitCfg: {
                                width: 110
                            },
                            value: this.init
                        }
                    ]
                });
                break;
            }
            case 'changeProtocolDialectOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialectOfDeviceComTask', 'MDC', 'Change protocol dialect'));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.protocolDialect', 'MDC', 'Protocol dialect'),
                        name: 'name',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init
                    });
                break;
            }
            case 'changeUrgencyOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeUrgencyOfDeviceComTask', 'MDC', 'Change urgency'));
                this.down('#changeConnectionItemForm').add({
                    xtype: 'numberfield',
                    name: 'urgency',
                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.urgency', 'MDC', 'Urgency'),
                    value: this.init
                });
                break;
            }
        }
//        this.down('#scheduleField').on('schedulefieldupdated',function(){
//            me.center();
//        },me);
    }
});

