/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.ImportTrustedCertificate', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.trusted-certificate-import',
    requires: [
        'Pkj.view.ImportTrustedCertificateForm'
    ],

    trustStore: null,
    cancelLink: undefined,

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                //layout: 'fit',
                items: {
                    xtype: 'trusted-certificate-import-form',
                    autoEl: {
                        tag: 'form',
                        enctype: 'multipart/form-data'
                    },
                    record: this.record,
                    cancelLink: this.cancelLink
                }
            }
        ];
        this.callParent(arguments);
    }
});