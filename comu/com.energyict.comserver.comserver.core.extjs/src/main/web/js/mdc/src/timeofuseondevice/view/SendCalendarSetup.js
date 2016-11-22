Ext.define('Mdc.timeofuseondevice.view.SendCalendarSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-device-send-cal-setup',
    overflowY: true,
    device: null,
    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.timeofuseondevice.view.SendCalendarForm'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('timeofuse.sendTimeOfUseCalendar', 'MDC', 'Send time of use calendar'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'tou-device-send-cal-form',
                        deviceId: me.device.get('name')
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
                        itemId: 'deviceMenu',
                        device: me.device,
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
