/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.trusted-certificate-preview',
    requires: [
        'Pkj.view.TrustedCertificatePreviewForm',
        'Pkj.view.TrustedCertificateActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                //  privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'trusted-certificate-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'trusted-certificate-preview-form'
        };
        me.callParent(arguments);
    }

});