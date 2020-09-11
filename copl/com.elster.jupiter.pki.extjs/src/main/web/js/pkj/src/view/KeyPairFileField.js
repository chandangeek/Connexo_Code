Ext.define('Pkj.view.KeyPairFileField', {
    extend: 'Ext.form.field.File',
    xtype: 'key-pair-file-field',
    name: 'file',
    required: true,
    fieldLabel: Uni.I18n.translate('general.keyPairFile', 'PKJ', 'Key pair file'),
    allowBlank: false,
    buttonText: Uni.I18n.translate('general.selectFile', 'PKJ', 'Select file...'),
    afterBodyEl: [
        '<div class="x-form-display-field"><i>',
        Uni.I18n.translate('general.maxKeyPairFileSize', 'PKJ', 'File should be DER-encoded'),
        '</i></div>'
    ]
});
