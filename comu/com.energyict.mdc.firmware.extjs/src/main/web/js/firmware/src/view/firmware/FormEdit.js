/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    hydrator: 'Fwc.form.Hydrator',
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            margin: '0 0 10 0',
            anchor: '60%',
            hidden: true
        },
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            itemId: 'text-firmware-version',
            anchor: '60%',
            required: true,
            fieldLabel: Uni.I18n.translate('general.version', 'FWC', 'Version'),
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
            itemId: 'firmware-field-file',
            required: false,
            allowBlank: true,
            afterBodyEl: [
                '<div class="x-form-display-field"><i>',
                Uni.I18n.translate('firmware.filesize.edit', 'FWC', 'The selected file will replace the already uploaded firmware file. Maximum file size is 150MB'),
                '</i></div>'
            ],
            anchor: '60%'
        },
        {
            xtype: 'textfield',
            itemId: 'text-image-identifier',
            name: 'imageIdentifier',
            fieldLabel: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
            required: true,
            anchor: '60%'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-type',
            fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            name: 'type'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-status',
            fieldLabel: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            name: 'status'
        },
        {
            xtype: 'hiddenfield',
            name: 'version'
        }
    ]
});