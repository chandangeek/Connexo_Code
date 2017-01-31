/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        router: this.router,
                        deviceTypeId: this.deviceType.get('id')
                    }
                ]
            }
        ];

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


