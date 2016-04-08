Ext.define('Fwc.view.firmware.field.File', {
    extend: 'Ext.form.field.File',
    xtype: 'firmware-field-file',
    name: 'firmwareFile',
    required: true,
    fieldLabel: Uni.I18n.translate('firmware.file.label', 'FWC', 'Firmware file'),
    allowBlank: false,
    buttonText: Uni.I18n.translate('firmware.file.select', 'FWC', 'Select file...'),
    afterBodyEl: [
        '<div class="x-form-display-field"><i>',
        Uni.I18n.translate('firmware.filesize', 'FWC', 'Maximum file size is 150MB'),
        '</i></div>'
    ]
});
