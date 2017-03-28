/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateFileField', {
    extend: 'Ext.form.field.File',
    xtype: 'certificate-file-field',
    name: 'file',
    required: true,
    fieldLabel: Uni.I18n.translate('general.certificateFile', 'PKJ', 'Certificate file'),
    allowBlank: false,
    buttonText: Uni.I18n.translate('general.selectFile', 'PKJ', 'Select file...'),
    afterBodyEl: [
        '<div class="x-form-display-field"><i>',
        Uni.I18n.translate('general.maxKeyStoreFileSize', 'PKJ', 'Maximum file size is 2 kB'),
        '</i></div>'
    ]
});
