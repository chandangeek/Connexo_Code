/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatePreviewForm', {
    extend: 'Ext.form.Panel',
    frame: false,
    layout: 'fit',
    alias: 'widget.trusted-certificate-preview-form',

    requires: [
        'Uni.util.FormEmptyMessage'
    ],

    items: [
        {
            defaults: {
                xtype: 'displayfield'
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                    name: 'alias'
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                    name: 'state'
                }
            ]
        }
    ]
});
