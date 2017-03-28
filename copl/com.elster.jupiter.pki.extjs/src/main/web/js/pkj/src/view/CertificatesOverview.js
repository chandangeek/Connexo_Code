/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.certificates-overview',
    router: null,

    requires: [
        'Pkj.view.CertificatesGrid',
        'Pkj.view.CertificatePreview',
        'Pkj.view.CertificateFilter',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],

    store: undefined,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.certificates', 'PKJ', 'Certificates'),
                items: [
                    {
                        xtype: 'certificateFilter'
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'certificates-grid',
                            itemId: 'pkj-certificates-grid',
                            store: me.store,
                            router: me.router

                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'pkj-no-certificates-found',
                            title: Uni.I18n.translate('certificates.empty.title', 'PKJ', 'No certificates found'),
                            reasons: [
                                Uni.I18n.translate('certificates.empty.list.reason1', 'PKJ', 'No certificates have been defined yet.'),
                                Uni.I18n.translate('certificates.empty.list.reason2', 'PKJ', 'No certificates comply with the filter.')
                            ],
                            stepItems: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.addCSR', 'PKJ', 'Add CSR'),
                                    itemId: 'pkj-no-certificates-add-csr-btn'
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.addCertificate', 'PKJ', 'Add certificate'),
                                    itemId: 'pkj-no-certificates-add-certificate-btn'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'certificate-preview',
                            itemId: 'pkj-certificate-preview'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
