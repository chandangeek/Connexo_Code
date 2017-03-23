/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.certificate-preview',
    requires: [
        'Pkj.view.TrustedCertificatePreviewForm',
        'Pkj.view.TrustedCertificateActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.items = {
            xtype: 'form',
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                    name: 'alias'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                    name: 'state'
                }
            ]
        };
        me.callParent(arguments);
    }

});