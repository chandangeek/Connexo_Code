/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.truststore-certificates-view',

    requires: [
        'Pkj.view.TrustedCertificatesGrid',
        'Pkj.view.TrustedCertificatePreview',
        'Pkj.view.TrustStorePreviewForm',
        'Pkj.view.TrustStoreActionMenu',
        'Pkj.privileges.CertificateManagement',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Pkj.view.TrustedCertificateFilter'
    ],

    store: undefined,
    trustStoreId: undefined,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            itemId: 'pkj-truststore-certificates-view',
            title: ' ',
            items: [
                {
                    xtype: 'container',
                    itemId: 'pkj-truststore-certificates-container',
                    items: [
                        {
                            xtype: 'panel',
                            padding: '0 0 0 0',
                            title: Uni.I18n.translate('general.details', 'PKJ', 'Details'),
                            ui: 'medium',
                            tools: [
                                {
                                    xtype: 'uni-button-action',
                                    privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                                    menu: {
                                        xtype: 'truststore-action-menu',
                                        itemId: 'pkj-truststore-certificates-view-menu'
                                    }
                                }
                            ],
                            items: [
                                {
                                    xtype: 'form',
                                    itemId: 'pkj-truststore-certificates-view-form',
                                    items: [
                                        {
                                            xtype: 'truststore-preview-form'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'panel',
                            padding: '0 0 0 0',
                            title: Uni.I18n.translate('general.trustedCertificates', 'PKJ', 'Trusted certificates'),
                            ui: 'medium',
                            items: [
                                {
                                    xtype: 'trustedCertificateFilter',
                                    trustStoreId: me.trustStoreId
                                },
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
                                            Uni.I18n.translate('trustedCertificates.empty.list.reason1', 'PKJ', 'No trusted certificates have been defined yet.'),
                                            Uni.I18n.translate('trustedCertificates.empty.list.reason2', 'PKJ', 'No trusted certificates comply with the filter.')
                                        ],
                                        stepItems: [
                                            {
                                                text: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                                                privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                                                xtype: 'button',
                                                itemId: 'pkj-import-certificates-step'
                                            }
                                        ]
                                    },
                                    previewComponent: {
                                        xtype: 'trusted-certificate-preview',
                                        itemId: 'pkj-certificate-preview'
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    loadTrustStoreRecord: function(trustStoreRecord) {
        var me = this;

        me.down('#pkj-truststore-certificates-view').setTitle(trustStoreRecord.get('name'));
        me.down('#pkj-truststore-certificates-view-form').loadRecord(trustStoreRecord);
        me.down('#pkj-truststore-certificates-container panel').tools[0].menu.record = trustStoreRecord;
    }

});
