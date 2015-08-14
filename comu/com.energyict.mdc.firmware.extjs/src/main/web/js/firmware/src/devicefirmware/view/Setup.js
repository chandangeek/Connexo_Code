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

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [{
                    xtype: 'deviceMenu',
                    router: me.router,
                    device: me.device
                }]
            }
        ];

        me.content = {
            ui: 'large',
            itemId: 'device-firmwares',
            title: Uni.I18n.translate('general.firmware', 'FWC', 'Firmware'),
            defaults: {
                margin: '0 0 20 0'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            }
        };

        me.callParent(arguments);
    }
});
