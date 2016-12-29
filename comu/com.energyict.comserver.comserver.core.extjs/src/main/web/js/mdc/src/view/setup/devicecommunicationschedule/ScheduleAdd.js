Ext.define('Mdc.view.setup.devicecommunicationschedule.ScheduleAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-schedule-add',

    device: undefined,
    comTaskId: undefined,
    title: Uni.I18n.translate('deviceCommunicationPlanning.addSchedule', 'MDC', 'Add schedule'),
    editMode: false,
    comTask: undefined, // (only) used in case editMode = true; type=Mdc.model.CommunicationTaskSimple

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'device-command-add-form',
                title: me.title,
                layout: {
                    type: 'vbox',
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
                        required: true,
                        msgTarget: 'under',
                        items: [
                            {
                                xtype: 'scheduleField',
                                name: 'schedule',
                                itemId: 'device-schedule-add-scheduleField',
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
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: me.editMode ? Uni.I18n.translate('general.save', 'MDC', 'Save') : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: me.editMode ? 'editScheduleAction' : 'addScheduleAction',
                                itemId: 'device-schedule-add-addButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'device-schedule-add-cancelLink'
                            }
                        ]
                    }
                ]
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId: 'communicationPlanningLink',
                        device: me.device
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});