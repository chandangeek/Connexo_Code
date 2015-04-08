Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    hydrator: 'Fwc.form.Hydrator',
    items: [
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            anchor: '60%',
            required: true,
            fieldLabel: 'Version',
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
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
            fieldLabel: 'Firmware type',
            name: 'type'
        },
        {
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            name: 'status'
        }
    ]
});