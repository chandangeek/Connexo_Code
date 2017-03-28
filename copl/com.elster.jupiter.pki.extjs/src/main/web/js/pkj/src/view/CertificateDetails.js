/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateDetails', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.certificate-details',
    requires: [
        'Pkj.view.CertificatePreview'
    ],

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                items: {
                    xtype: 'certificate-preview',
                    frame: false
                }
            }
        ];
        this.callParent(arguments);
    }
});