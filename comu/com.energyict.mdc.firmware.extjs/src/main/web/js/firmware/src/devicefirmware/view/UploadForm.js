/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.devicefirmware.view.UploadForm', {
    extend: 'Ext.form.Panel',
    xtype: 'device-firmware-upload-form',
    itemId: 'device-firmware-upload-form',
    ui: 'large',
    defaults: {
        labelWidth: 250
    },
    minButtonWidth: 50,
    requires: [
        'Uni.property.form.Property',
        'Fwc.devicefirmware.view.form.UploadFieldContainer'
    ],
    record: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'upload-field-container',
                groupName: 'uploadFileContainer',
                itemId: 'uploadFileField',
                fieldLabel: Uni.I18n.translate('deviceFirmware.uploadFile', 'FWC', 'Upload file'),
                margin: '0 0 20 0'
            }
        ];

        me.buttons = [
            {
                text: Uni.I18n.translate('general.confirm', 'FWC', 'Confirm'),
                ui: 'action',
                action: 'uploadFirmware',
                itemId: 'uploadBtn'
            },
            {
                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                ui: 'link',
                itemId: 'cancelLink',
                href: me.router.getRoute('devices/device/firmware').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});