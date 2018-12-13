/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.trusted-certificate-preview',
    requires: [
        'Pkj.view.TrustedCertificatePreviewForm',
        'Pkj.view.TrustedCertificateActionMenu',
        'Pkj.privileges.CertificateManagement'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                menu: {
                    xtype: 'trusted-certificate-action-menu',
                    itemId: 'pkj-trusted-certificate-preview-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'trusted-certificate-preview-form'
        };
        me.callParent(arguments);
    }

});