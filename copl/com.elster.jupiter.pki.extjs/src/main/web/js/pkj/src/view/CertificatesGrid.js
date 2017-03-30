/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.CertificatesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.certificates-grid',
    store: undefined,
    requires: [
        'Uni.grid.column.Action',
        'Pkj.view.CertificateActionMenu',
        'Pkj.view.CertificatesGridActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    router: undefined,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.alias', 'PKJ', 'Alias'),
                dataIndex: 'alias',
                flex: 1,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/certificates/view').buildUrl({certificateId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.keyusage', 'PKJ', 'Key usage'),
                dataIndex: 'type',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.issuer', 'PKJ', 'Issuer'),
                dataIndex: 'issuer',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.subject', 'PKJ', 'Subject'),
                dataIndex: 'subject',
                flex: 2
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
                width: 150,
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                menu: {
                    xtype: 'certificate-action-menu',
                    itemId: 'pkj-certificates-grid-certificate-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('certificates.pagingtoolbartop.displayMsg', 'PKJ', '{0} - {1} of {2} certificates'),
                displayMoreMsg: Uni.I18n.translate('certificates.pagingtoolbartop.displayMoreMsg', 'PKJ', '{0} - {1} of more than {2} certificates'),
                emptyMsg: Uni.I18n.translate('certificates.pagingtoolbartop.emptyMsg', 'PKJ', 'There are no certificates to display'),
                items: [
                    {
                        xtype: 'uni-button-action',
                        privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                        menu: {
                            xtype: 'certificates-grid-action-menu',
                            itemId: 'pkj-certificates-grid-action-menu'
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('certificates.pagingtoolbarbottom.itemsPerPage', 'PKJ', 'Certificates per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});