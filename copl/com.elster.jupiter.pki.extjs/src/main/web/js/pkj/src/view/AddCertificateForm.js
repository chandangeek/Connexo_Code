/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.AddCertificateForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.certificate-add-form',
    edit: false,

    requires: [
        'Pkj.view.CertificateFileField',
        'Uni.util.FormErrorMessage'
    ],
    layout: 'anchor',
    margin: '15 0 0 0',

    cancelLink: undefined,
    trustStoreRecord: undefined,
    importMode: false,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'pkj-certificate-add-form-errors',
                margin: '0 0 10 0',
                anchor: '40%',
                hidden: true
            },
            {
                xtype: 'textfield',
                fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                required: true,
                name: 'alias',
                itemId: 'pkj-certificate-add-form-alias',
                allowBlank: !me.importMode,
                anchor: '40%',
                hidden: me.importMode
            },
            {
                xtype: 'certificate-file-field',
                itemId: 'pkj-certificate-add-form-file',
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
                        text: me.importMode
                            ? Uni.I18n.translate('general.import', 'PKJ', 'Import')
                            : Uni.I18n.translate('general.add', 'PKJ', 'Add'),
                        ui: 'action',
                        itemId: 'pkj-certificate-add-form-add-btn'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel', 'PKJ', 'Cancel'),
                        ui: 'link',
                        itemId: 'pkj-certificate-add-form-cancel-link',
                        href: me.cancelLink
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});