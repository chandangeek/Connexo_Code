Ext.define('Fwc.view.firmware.FirmwareEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-edit',
    itemId: 'firmware-edit',
    requires: [
        'Fwc.view.firmware.FormEdit'
    ],
    deviceType: null,

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: this.router.getRoute().getTitle(),
                layout: 'fit',

                items: {
                    xtype: 'firmware-form-edit',
                    record: this.record,
                    router: this.router
                }
            }
        ];

        this.callParent(arguments);
    }
});


