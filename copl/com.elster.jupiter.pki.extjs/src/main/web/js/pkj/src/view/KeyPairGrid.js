/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.KeyPairGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.key-pair-grid',
    store: undefined,
    requires: [
        'Uni.grid.column.Action',
        'Pkj.view.KeyPairActionMenu',
        'Pkj.view.KeyPairGridActionMenu',
        'Pkj.privileges.CertificateManagement',
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
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.availableKeys', 'PKJ', 'Available keys'),
                dataIndex: 'hasPublicKey',
                flex: 2,
                renderer: function(data,a,keypair){
                    var result = '-';
                    if(keypair){
                        if(keypair.get('hasPublicKey') && keypair.get('hasPrivateKey')) {
                            result = Uni.I18n.translate('general.hasPrivatePublicKey', 'PKJ', 'Private key and public key')
                        } else {
                            if(keypair.get('hasPublicKey')){
                                result = Uni.I18n.translate('general.hasPublicKey', 'PKJ', 'Public key')
                            }
                            if(keypair.get(hasPrivateKey)){
                                result = Uni.I18n.translate('general.hasPrivateKey', 'PKJ', 'Private key')
                            }
                        }
                    }
                    return result;
                }
            },
            {
                header: Uni.I18n.translate('general.storgaMethod', 'PKJ', 'Storage method'),
                dataIndex: 'keyEncryptionMethod',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.keyType', 'PKJ', 'Key type'),
                dataIndex: 'keyType',
                renderer: function(data){
                    return data.name;
                },
                flex: 2
            },
            {
                xtype: 'uni-actioncolumn',
                width: 150,
                privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                menu: {
                    xtype: 'key-pair-action-menu',
                    itemId: 'pkj-key-pair-grid-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('keypairs.pagingtoolbartop.displayMsg', 'PKJ', '{0} - {1} of {2} key pairs'),
                displayMoreMsg: Uni.I18n.translate('keypairs.pagingtoolbartop.displayMoreMsg', 'PKJ', '{0} - {1} of more than {2} key pairs'),
                emptyMsg: Uni.I18n.translate('keypairs.pagingtoolbartop.emptyMsg', 'PKJ', 'There are no key pairs to display'),
                items: [
                    {
                        xtype: 'uni-button-action',
                        privileges: Pkj.privileges.CertificateManagement.adminCertificates,
                        menu: {
                            xtype: 'key-pair-grid-action-menu',
                            itemId: 'pkj-key-pair-grid-action-menu'
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('keypairs.pagingtoolbarbottom.itemsPerPage', 'PKJ', 'Key pairs per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});