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
        Uni.I18n.translate('general.maxCertificateFileSize', 'PKJ', 'File should be DER-encoded'),
        '</i></div>'
    ]
});
