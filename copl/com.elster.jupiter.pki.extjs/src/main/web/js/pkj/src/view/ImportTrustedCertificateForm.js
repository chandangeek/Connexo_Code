/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.ImportTrustedCertificateForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.trusted-certificate-import-form',
    edit: false,

    requires: [
        'Pkj.view.KeyStoreFileField',
        'Uni.util.FormErrorMessage',
        'Uni.util.FormInfoMessage'
    ],
    layout: 'anchor',
    margin: '15 0 0 0',

    cancelLink: undefined,
    trustStoreRecord: undefined,
    showInfoMsg: undefined,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'pkj-trusted-certificate-import-form-errors',
                margin: '0 0 10 0',
                anchor: '40%',
                hidden: true
            },
            {
                xtype: 'panel',
                itemId: 'pkj-trusted-certificate-import-form-info',
                hidden: !me.showInfoMsg,
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: Uni.I18n.translate('general.certificatesWithSameAlias.info', 'PKJ', 'The existing certificates with the same alias will be overwritten.')
                    }
                ]
            },
            {
                xtype: 'keystore-file-field',
                itemId: 'pkj-trusted-certificate-import-form-file',
                anchor: '40%'
            },
            {
                xtype: 'password-field',
                fieldLabel: Uni.I18n.translate('general.password', 'PKJ', 'Password'),
                passwordAsTextComponent: true,
                required: true,
                name: 'password',
                itemId: 'pkj-trusted-certificate-import-form-password',
                allowBlank: false,
                enforceMaxLength: true,
                maxLength: 80,
                anchor: '40%'
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: '&nbsp;',
                anchor: '40%',
                layout: {
                    type: 'hbox'
                },
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.import', 'PKJ', 'Import'),
                        ui: 'action',
                        itemId: 'pkj-trusted-certificate-import-form-import-btn'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                        ui: 'link',
                        itemId: 'pkj-trusted-certificate-import-form-cancel-link',
                        href: me.cancelLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});