/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStoresOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.truststores-overview',
    router: undefined,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Pkj.view.TrustStoresGrid',
        'Pkj.view.TrustStorePreview',
        'Pkj.view.TrustStoreActionMenu'
    ],
    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.trustStores', 'PKJ', 'Trust stores'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'truststores-grid',
                        router: me.router,
                        itemId: 'pkj-truststores-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'pkj-no-truststores',
                        title: Uni.I18n.translate('trustStores.empty.title', 'PKJ', 'No trust stores found'),
                        reasons: [
                            Uni.I18n.translate('trustStores.empty.list.item1', 'PKJ', 'No trust stores have been defined yet.'),
                            Uni.I18n.translate('trustStores.empty.list.item2', 'PKJ', "Trust stores exist, but you don't have permission to view them.")
                        ],
                        stepItems: [
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('trustStores.add', 'PKJ', 'Add trust store'),
                                privileges: Pkj.privileges.CertificateManagement.adminTrustStores,
                                itemId: 'pkj-add-truststore-btn',
                                action: 'addTrustStore'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'truststore-preview',
                        itemId: 'pkj-truststore-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});