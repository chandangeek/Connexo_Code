Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    hydrator: 'Fwc.form.Hydrator',
    items: [
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            itemId: 'text-firmware-version',
            anchor: '60%',
            required: true,
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
            itemId: 'firmware-field-file',
            required: false,
            allowBlank: true,
            afterBodyEl: [
                '<div class="x-form-display-field"><i>',
                Uni.I18n.translate('firmware.filesize.edit', 'FWC', 'Selected file will replace already uploaded firmware file. Maximum file size is 50Mb'),
                '</i></div>'
            ],
            anchor: '60%'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-type',
            fieldLabel: 'Firmware type',
            name: 'type'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-status',
            fieldLabel: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            name: 'status'
        }
    ]
});