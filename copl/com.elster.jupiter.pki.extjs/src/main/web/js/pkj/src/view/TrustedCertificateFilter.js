/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificateFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Pkj.store.TrustedCertificates',
    xtype: 'trustedCertificateFilter',

    requires:[
        'Pkj.store.TrustedCertificateIssuers',
        'Pkj.store.TrustedCertificateAliases',
        'Pkj.store.TrustedCertificateKeyUsages',
        'Pkj.store.TrustedCertificateSubjects',
        'Pkj.store.TrustedCertificateStatuses'
    ],

    trustStoreId: undefined,

    initComponent: function () {
        var me = this,
            aliasStore = Ext.getStore('Pkj.store.TrustedCertificateAliases') || Ext.create('Pkj.store.TrustedCertificateAliases'),
            keyUsageStore = Ext.getStore('Pkj.store.TrustedCertificateKeyUsages') || Ext.create('Pkj.store.TrustedCertificateKeyUsages'),
            issuersStore = Ext.getStore('Pkj.store.TrustedCertificateIssuers') || Ext.create('Pkj.store.TrustedCertificateIssuers'),
            subjectsStore = Ext.getStore('Pkj.store.TrustedCertificateSubjects') || Ext.create('Pkj.store.TrustedCertificateSubjects'),
            statusStore = Ext.getStore('Pkj.store.TrustedCertificateStatuses') || Ext.create('Pkj.store.TrustedCertificateStatuses');

        aliasStore.getProxy().setUrl(me.trustStoreId);
        keyUsageStore.getProxy().setUrl(me.trustStoreId);
        issuersStore.getProxy().setUrl(me.trustStoreId);
        subjectsStore.getProxy().setUrl(me.trustStoreId);
        statusStore.getProxy().setUrl(me.trustStoreId);
        me.filters = [
            {
                type: 'combobox',
                dataIndex: 'alias',
                emptyText: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                multiSelect: true,
                displayField: 'alias',
                valueField: 'alias',
                store: aliasStore
            },
            {
                type: 'combobox',
                dataIndex: 'keyUsages',
                emptyText: Uni.I18n.translate('general.keyUsage', 'PKJ', 'Key usage'),
                multiSelect: true,
                displayField: 'keyUsage',
                valueField: 'keyUsage',
                store: keyUsageStore
            },
            {
                type: 'combobox',
                dataIndex: 'issuer',
                emptyText: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                multiSelect: true,
                displayField: 'issuer',
                valueField: 'issuer',
                store: issuersStore
            },
            {
                type: 'combobox',
                dataIndex: 'subject',
                emptyText: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                multiSelect: true,
                displayField: 'subject',
                valueField: 'subject',
                store: subjectsStore
            },
            {
                type: 'combobox',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: statusStore
            },
            {
                type: 'interval',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalFrom',
                dataIndexTo: 'intervalTo',
                text: Uni.I18n.translate('general.expirationDate', 'PKJ', 'Expiration date')
            }
        ];

        me.callParent(arguments);
    }
});
