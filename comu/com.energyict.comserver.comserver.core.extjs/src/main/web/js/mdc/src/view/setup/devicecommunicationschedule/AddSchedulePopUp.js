Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSchedulePopUp', {
    extend: 'Ext.window.Window',
    alias: 'widget.addSchedulePopUp',
    requires: [
        'Mdc.widget.ScheduleField'
    ],
   // plain: true,
    border: true,
    itemId: 'addSchedulePopUp',
    shadow: true,
    items: [
        {
            xtype: 'container',
            margin: '10px',
            items: [
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'addScheduleForm',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 250
                    },
                    items: [
                        {
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
                                    value: {
                                        every: {
                                            count: 15,
                                            timeUnit: 'minutes'
                                        },
                                        lastDay: false,
                                        offset: {
                                            count: 0,
                                            timeUnit: 'seconds'
                                        }
                                    }
                                }
                            ]
                        }
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
                            action: 'addIndividualScheduleAction',
                            itemId: 'scheduleButton',
                            flex:0
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            handler: function(button){
                                button.up('.window').close();
                            },
                            flex:0
                        }
                    ]
                }
            ]
        }

    ],
    initComponent: function(){
        var me =this;
        this.callParent(arguments);
        this.down('#scheduleButton').action = this.action;
        this.down('#scheduleField').on('schedulefieldupdated',function(){
            me.center();
        },me);
    }
});
