Ext.define('Fwc.devicefirmware.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-setup',

    requires: [
        'Fwc.devicefirmware.view.ActionMenu',
        'Mdc.view.setup.device.DeviceMenu'
    ],

    router: null,
    device: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceMenu',
            router: me.router,
            device: me.device
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('firmware.route.devicefirmware', 'FWC', 'Firmware'),
            items: [
                {
                    xtype: 'button',
                    text: 'Activate firmware',
                    action: 'activateFirmware'
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.actions', 'FWC', Uni.I18n.translate('general.actions', 'FWC', 'Actions')),
                    iconCls: 'x-uni-action-iconD',
                    menu: {
                        xtype: 'device-firmware-action-menu'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
