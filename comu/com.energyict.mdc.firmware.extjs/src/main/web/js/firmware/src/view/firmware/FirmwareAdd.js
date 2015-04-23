Ext.define('Fwc.view.firmware.FirmwareAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-add',
    itemId: 'firmware-add',
    requires: [
        'Fwc.view.firmware.FormAdd'
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
                    xtype: 'firmware-form-add',
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    record: this.record,
                    router: this.router
                }
            }
        ];

        this.callParent(arguments);
    }
});


