/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.truststores-certificates-view',

    requires: [
        'Pkj.view.TrustedCertificatesGrid',
        'Pkj.view.TrustedCertificatePreview',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    store: undefined,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.trustedCertificates', 'PKJ', 'Trusted certificates'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'truststores-certificates-grid',
                        router: me.router,
                        store: me.store,
                        itemId: 'pkj-truststores-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'pkj-no-certificates',
                        title: Uni.I18n.translate('trustedCertificates.empty.title', 'PKJ', 'No trusted certificates found'),
                        reasons: [
                            Uni.I18n.translate('trustedCertificates.empty.list.reason1', 'PKJ', 'No trusted certificates have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.importTrustedCertificate', 'PKJ', 'Import trusted certificate'),
                                xtype: 'button',
                                itemId: 'pkj-add-certificate-btn',
                                action: 'addTrustedCertificate'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'trusted-certificate-preview',
                        itemId: 'pkj-certificate-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }

});
