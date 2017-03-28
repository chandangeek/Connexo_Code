/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustedCertificatesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.truststores-certificates-grid',
    store: undefined,
    requires: [
        'Uni.grid.column.Action',
        //'Pkj.view.TrustStoreActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                dataIndex: 'alias',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.keyUsage', 'PKJ', 'Key usage'),
                dataIndex: 'type',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                dataIndex: 'issuer',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                dataIndex: 'subject',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'PKJ', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.expirationDate', 'PKJ', 'Expiration date'),
                dataIndex: 'expirationDate',
                flex: 1,
                renderer: function(value){
                    if (Ext.isEmpty(value)) {
                        return '-';
                    }
                    return Uni.DateTime.formatDateShort(new Date(value));
                }
            },
            {
                xtype: 'uni-actioncolumn',
                width: '150',
                //privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'trusted-certificate-action-menu',
                    itemId: 'pkj-truststore-detail-view-certificate-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('trustedCertificates.pagingtoolbartop.displayMsg', 'PKJ', '{0} - {1} of {2} trusted certificates'),
                displayMoreMsg: Uni.I18n.translate('trustedCertificates.pagingtoolbartop.displayMoreMsg', 'PKJ', '{0} - {1} of more than {2} trusted certificates'),
                emptyMsg: Uni.I18n.translate('trustedCertificates.pagingtoolbartop.emptyMsg', 'PKJ', 'There are no trusted certificates to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.importTrustedCertificates', 'PKJ', 'Import trusted certificates'),
                        itemId: 'pkj-certificates-grid-import-certificates'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('trustedCertificates.pagingtoolbarbottom.itemsPerPage', 'PKJ', 'Trusted certificates per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});