Ext.define('Fwc.devicefirmware.view.Upload', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-upload',

    title: null,
    route: null,
    requires: [
        'Fwc.devicefirmware.view.UploadForm'
    ],

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceMenu',
            router: me.router,
            device: me.device
        };

        me.content = {
            ui: 'large',
            title: me.title,
            items: [
                {
                    xtype: 'device-firmware-upload-form',
                    router: me.router
                }
            ]
        };

        me.callParent(arguments);
    }
});
