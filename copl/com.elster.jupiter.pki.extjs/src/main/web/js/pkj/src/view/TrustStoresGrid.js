/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStoresGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.truststores-grid',
    store: 'Pkj.store.TrustStores',
    requires: [
        'Uni.grid.column.Action',
        'Pkj.view.TrustStoreActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'PKJ', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                // privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'truststore-action-menu',
                    itemId: 'pkj-truststore-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('trustStores.pagingtoolbartop.displayMsg', 'PKJ', '{0} - {1} of {2} trust stores'),
                displayMoreMsg: Uni.I18n.translate('trustStores.pagingtoolbartop.displayMoreMsg', 'PKJ', '{0} - {1} of more than {2} trust stores'),
                emptyMsg: Uni.I18n.translate('trustStores.pagingtoolbartop.emptyMsg', 'PKJ', 'There are no trust stores to display'),
                items: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'pkj-truststores-grid-actions-btn',
                        text: Uni.I18n.translate('general.actions', 'PKJ', 'Actions'),
                        //privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                        //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.connectionMethodsActions,
                        //hidden: true,
                        menu: {
                            plain: true,
                            items: [
                                {
                                    text: Uni.I18n.translate('general.addTrustStore', 'PKJ', 'Add trust store'),
                                    itemId: 'pkj-truststores-grid-add-truststore'
                                },
                                {
                                    text: Uni.I18n.translate('general.importTrustStore', 'PKJ', 'Import trust store'),
                                    itemId: 'pkj-truststores-grid-import-truststore'
                                }
                            ]
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('trustStores.pagingtoolbarbottom.itemsPerPage', 'PKJ', 'trust stores per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});