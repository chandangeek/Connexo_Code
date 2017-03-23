/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.KeyStoreFileField', {
    extend: 'Ext.form.field.File',
    xtype: 'keystore-file-field',
    name: 'keyStoreFile',
    required: true,
    fieldLabel: Uni.I18n.translate('general.keyStoreFile', 'PKJ', 'Key store file'),
    allowBlank: false,
    buttonText: Uni.I18n.translate('general.selectFile', 'PKJ', 'Select file...'),
    afterBodyEl: [
        '<div class="x-form-display-field"><i>',
        Uni.I18n.translate('general.maxKeyStoreFileSize', 'PKJ', 'Maximum file size is 250kB'),
        '</i></div>'
    ]
});
