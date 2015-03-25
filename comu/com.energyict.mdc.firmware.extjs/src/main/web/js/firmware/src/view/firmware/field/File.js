Ext.define('Fwc.view.firmware.field.File', {
    extend: 'Ext.form.field.File',
    alias: 'widget.firmware-field-file',
    name: 'firmwareFile',
    required: true,
    fieldLabel: 'Firmware file',
    allowBlank: false,
    buttonText: 'Select file...',
    afterBodyEl: [
        '<div class="x-form-display-field"><i>',
        Uni.I18n.translate('firmware.filesize', 'FWC', 'Maximum file size is 50Mb'),
        '</i></div>'
    ]
});
