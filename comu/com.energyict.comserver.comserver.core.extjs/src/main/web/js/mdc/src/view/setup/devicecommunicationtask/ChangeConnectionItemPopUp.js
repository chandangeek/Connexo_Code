Ext.define('Mdc.view.setup.devicecommunicationtask.ChangeConnectionItemPopUp', {
    extend: 'Ext.window.Window',
    alias: 'widget.changeConnectionItemPopUp',
    requires: [
        'Mdc.widget.ScheduleField'
    ],
    width: 500,
    height: 200,
    closable: false,
    autoShow: true,
    modal: true,
    floating: true,
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
            items: [
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'changeConnectionItemForm',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                    ]
                }

            ]
        }
    ],

    bbar: [

                {
                    xtype: 'container',
                    flex: 1
                },
                {
                    text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
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
        Ext.suspendLayouts();
        switch (this.action) {
            case 'changeConnectionMethodOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeConnectionMethodOf', 'MDC', "Change connection method of '{0}'", [this.comTaskName]));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.connectionMethod', 'MDC', 'Connection method'),
                        name: 'name',
                        itemId: 'connectionMethodCombo',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init });
                break;
            }
            case 'changeFrequencyOfDeviceComTask':
            {
                this.setTitle(Ext.String.format(Uni.I18n.translate('deviceCommunicationTask.changeFrequencyOfDeviceComTask', 'MDC', 'Change frequency of \'{0}\''),this.comTaskName));
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
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeProtocolDialectOfDeviceComTask', 'MDC', "Change protocol dialect of {0}", [this.comTaskName]));
                this.down('#changeConnectionItemForm').add(
                    {
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('deviceCommunicationTask.protocolDialect', 'MDC', 'Protocol dialect'),
                        name: 'name',
                        itemId: 'protocolDialectCombo',
                        displayField: 'name',
                        store: this.store,
                        queryMode: 'local',
                        value: this.init
                    });
                break;
            }
            case 'changeUrgencyOfDeviceComTask':
            {
                this.setTitle(Uni.I18n.translate('deviceCommunicationTask.changeUrgencyOfDeviceComTask', 'MDC', 'Change urgency of {0}', [this.comTaskName]));
                this.down('#changeConnectionItemForm').add({
                    xtype: 'numberfield',
                    itemId: 'urgencyCombo',
                    name: 'urgency',
                    fieldLabel: Uni.I18n.translate('deviceCommunicationTask.urgency', 'MDC', 'Urgency'),
                    value: this.init
                });
                break;
            }
        }
        Ext.resumeLayouts();
    }
});

