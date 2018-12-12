/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificateFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    store: 'Pkj.store.Certificates',
    xtype: 'certificateFilter',

    requires:[
        'Pkj.store.CertificateIssuers',
        'Pkj.store.CertificateAliases',
        'Pkj.store.CertificateKeyUsages',
        'Pkj.store.CertificateSubjects',
        'Pkj.store.CertificateStatuses'
    ],

    initComponent: function () {
        var me = this;

        me.filters = [
            {
                type: 'combobox',
                itemId: 'certificate-filter-alias',
                dataIndex: 'alias',
                emptyText: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                multiSelect: true,
                displayField: 'alias',
                valueField: 'alias',
                store: 'Pkj.store.CertificateAliases'
            },
            {
                type: 'combobox',
                itemId: 'certificate-filter-keyUsages',
                dataIndex: 'keyUsages',
                emptyText: Uni.I18n.translate('general.keyUsage', 'PKJ', 'Key usage'),
                multiSelect: true,
                displayField: 'keyUsage',
                valueField: 'keyUsage',
                store: 'Pkj.store.CertificateKeyUsages'
            },
            {
                type: 'combobox',
                itemId: 'certificate-filter-issuer',
                dataIndex: 'issuer',
                emptyText: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                multiSelect: true,
                displayField: 'issuer',
                valueField: 'issuer',
                store: 'Pkj.store.CertificateIssuers'
            },
            {
                type: 'combobox',
                itemId: 'certificate-filter-subject',
                dataIndex: 'subject',
                emptyText: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                multiSelect: true,
                displayField: 'subject',
                valueField: 'subject',
                store: 'Pkj.store.CertificateSubjects'
            },
            {
                type: 'combobox',
                itemId: 'certificate-filter-status',
                dataIndex: 'status',
                emptyText: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                multiSelect: true,
                displayField: 'name',
                valueField: 'id',
                store: 'Pkj.store.CertificateStatuses'
            },
            {
                type: 'interval',
                itemId: 'certificate-filter-interval',
                dataIndex: 'interval',
                dataIndexFrom: 'intervalFrom',
                dataIndexTo: 'intervalTo',
                text: Uni.I18n.translate('general.expirationDate', 'PKJ', 'Expiration date')
            }
        ];

        me.callParent(arguments);
    }
});
