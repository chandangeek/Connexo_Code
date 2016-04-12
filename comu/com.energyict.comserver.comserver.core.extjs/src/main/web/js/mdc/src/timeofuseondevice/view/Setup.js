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
            ui: 'large',
            title: Uni.I18n.translate('general.timeOfUseCalendars', 'MDC', 'Time of use calendars'),
            layout: {
                type: 'vbox',
                align: 'stretch',
            },
            tools: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                    //privileges: Scs.privileges.ServiceCall.admin,
                    iconCls: 'x-uni-action-iconD',
                    itemId: 'tou-device-actions-button',
                    margin: '0 20 0 0',
                    menu: {
                        xtype: 'tou-device-action-menu'
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
                    ui: 'large',
                    xtype: 'device-tou-planned-on-form'
                }
            ]
        };

        me.callParent(arguments);
    }
});