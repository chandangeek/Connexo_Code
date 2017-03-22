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
                header: Uni.I18n.translate('general.expirationDate', 'PKJ', 'Expiration date'),
                dataIndex: 'expirationDate',
                flex: 1
            }
            //,
            //{
            //    xtype: 'uni-actioncolumn',
            //     privileges: Apr.privileges.AppServer.admin,
                //menu: {
                //    xtype: 'truststore-action-menu',
                //    itemId: 'pkj-truststore-action-menu'
                //}
            //}
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
                        text: Uni.I18n.translate('general.importTrustedCertificate', 'PKJ', 'Import trusted certificate'),
                        itemId: 'pkj-certificates-grid-import-certificate'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('trustStores.pagingtoolbarbottom.itemsPerPage', 'PKJ', 'Trusted certificates per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});