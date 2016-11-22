Ext.define('Mdc.timeofuseondevice.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-tou-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.timeofuseondevice.view.TimeOfUsePreviewForm',
        'Mdc.timeofuseondevice.view.TimeOfUsePlannedOnForm',
        'Mdc.timeofuseondevice.view.ActionMenu',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    device: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'deviceMenu',
                        device: me.device,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            itemId: 'wrappingPanel',
            ui: 'large',
            title: Uni.I18n.translate('general.timeOfUse', 'MDC', 'Time of use'),
            layout: {
                type: 'vbox',
                align: 'stretch',
            },
            tools: [
                {
                    xtype: 'uni-button-action',
                    privileges: Mdc.privileges.Device.viewDevice,
                    itemId: 'tou-device-actions-button',
                    margin: '0 20 0 0',
                    menu: {
                        xtype: 'tou-device-action-menu',
                        device: me.device
                    }
                }
            ],
            items: [
                {
                    margin: '-20 0 0 0',
                    ui: 'large',
                    xtype: 'device-tou-preview-form'
                },
                {
                    title: Uni.I18n.translate('general.plannedOn', 'MDC', 'Planned on'),
                    ui: 'medium',
                    cls: 'no-side-padding',
                    xtype: 'device-tou-planned-on-form'
                },
                {
                    xtype: 'no-items-found-panel',
                    title: Uni.I18n.translate('timeofUse.noCalendarsFound', 'MDC', 'No active time of use calendar found'),
                    reasons: [
                        Uni.I18n.translate('timeofUse.noCalendarsFound.list.item1', 'MDC', 'There is no active calendar.'),
                        Uni.I18n.translate('timeofUse.noCalendarsFound.list.item2', 'MDC', 'There is no planned calendar.'),
                        Uni.I18n.translate('timeofUse.noCalendarsFound.list.item3', 'MDC', 'There is an active calendar but you need to verify.')
                    ],
                    stepItems: [
                        {
                            text:Uni.I18n.translate('timeofuse.sendCalendar', 'MDC', 'Send calendar'),
                            privileges: Mdc.privileges.DeviceCommands.executeCommands,
                            itemId: 'empty-comp-send-calendar-tou',
                            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.supportsSend,
                            deviceId: me.device.get('name')
                        },
                        {
                            text: Uni.I18n.translate('timeofuse.checkTimeOfUseCalendar', 'MDC', 'Check time of use calendar'),
                            itemId: 'empty-comp-verify-calendars-tou',
                            privileges: Mdc.privileges.DeviceCommands.executeCommands,
                            deviceId: me.device.get('name')
                        }
                    ],
                    hidden: true
                }
            ]
        };

        me.callParent(arguments);
    },

    showEmptyComponent: function() {
        var me = this;
        if(me.down('#tou-device-actions-button')) {
            if(!Mdc.dynamicprivileges.DeviceState.canVerify() && !Mdc.dynamicprivileges.DeviceState.canSendCalendar()) {
                me.down('#tou-device-actions-button').hide();
            }
            me.down('device-tou-preview-form').hide();
            me.down('device-tou-planned-on-form').hide();
            me.down('no-items-found-panel').show();
            me.down('#wrappingPanel').setLoading(false);
        }
    }
});