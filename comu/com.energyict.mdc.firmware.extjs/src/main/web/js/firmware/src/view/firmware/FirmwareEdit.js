Ext.define('Fwc.view.firmware.FirmwareEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-edit',
    itemId: 'firmware-edit',
    requires: [
        'Fwc.view.firmware.FormEdit',
        'Fwc.view.firmware.FormEditGhost'
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
                    xtype:  (  this.record.getAssociatedData().firmwareStatus
                            && this.record.getAssociatedData().firmwareStatus.id === 'ghost')
                    ? 'firmware-form-edit-ghost'
                    : 'firmware-form-edit',
                    record: this.record,
                    router: this.router
                }
            }
        ];

        this.callParent(arguments);
    }
});


