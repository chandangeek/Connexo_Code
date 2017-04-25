/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateDetails', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.certificate-details',
    requires: [
        'Pkj.view.CertificatePreview',
        'Pkj.view.CertificateActionMenu',
        'Pkj.privileges.CertificateManagement',
        'Uni.button.Action'
    ],

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                itemId: 'pkj-certificate-details-panel',
                ui: 'large',
                tools: [
                    {
                        xtype: 'uni-button-action',
                        privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                        menu: {
                            xtype: 'certificate-action-menu'
                        }
                    }
                ],
                items: {
                    xtype: 'certificate-preview',
                    frame: false
                }
            }
        ];
        this.callParent(arguments);
    }
});