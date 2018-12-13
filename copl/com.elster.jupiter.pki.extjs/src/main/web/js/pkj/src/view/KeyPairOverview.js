/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.KeyPairOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.key-pair-overview',
    router: null,

    requires: [
         'Pkj.view.KeyPairGrid',
         'Pkj.view.KeyPairPreview',
         'Pkj.view.KeyPairActionMenu'
    ],

    store: undefined,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                ui: 'large',
                title: Uni.I18n.translate('general.keypairs', 'PKJ', 'Key pairs'),
                items: [
                    //{
                    //    xtype: 'certificateFilter'
                    //},e
                     {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'key-pair-grid',
                            itemId: 'pkj-key-pair-grid',
                            store: me.store,
                            router: me.router

                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'pkj-no-key-pairs-found',
                            title: Uni.I18n.translate('keypair.empty.title', 'PKJ', 'No key pairs found'),
                            reasons: [
                                Uni.I18n.translate('keypair.empty.list.reason1', 'PKJ', 'No key pairs have been defined yet.'),
                                Uni.I18n.translate('keypair.empty.list.reason2', 'PKJ', 'No key pairs comply with the filter.')
                            ],
                            stepItems: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.generateKeyPair', 'PKJ', 'Generate key pair'),
                                    privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                                    itemId: 'pkj-no-key-pair-generate-key-pair-btn'
                                },
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('general.importKeyPair', 'PKJ', 'Import key pair'),
                                    privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                                    itemId: 'pkj-no-key-pair-import-key-pair-btn'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'key-pair-preview',
                            itemId: 'pkj-key-pair-preview',
                            tools: [
                                {
                                    xtype: 'uni-button-action',
                                    privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                                    menu: {
                                        xtype: 'key-pair-action-menu',
                                        itemId: 'pkj-key-pair-preview-action-menu'
                                    }
                                }
                            ]
                        }
                     }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
