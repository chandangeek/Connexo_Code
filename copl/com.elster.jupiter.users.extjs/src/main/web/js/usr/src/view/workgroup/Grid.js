/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.workgroup.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usr-workgroups-grid',
    store: 'Usr.store.Workgroups',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Usr.privileges.Users'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'USR', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('workgroups.description', 'USR', 'Description'),
                dataIndex: 'description',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Usr.privileges.Users.admin,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    return !Usr.privileges.Users.canAdministrate();
                },
                menu: {
                    xtype: 'usr-workgroup-action-menu',
                    itemId: 'mnu-workgroup-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('workgroups.pagingtoolbartop.displayMsg', 'USR', '{0} - {1} of {2} workgroups'),
                displayMoreMsg: Uni.I18n.translate('workgroups.pagingtoolbartop.displayMoreMsg', 'USR', '{0} - {1} of more than {2} workgroups'),
                emptyMsg: Uni.I18n.translate('workgroups.pagingtoolbartop.emptyMsg', 'USR', 'There are no workgroups to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addWorkgroup', 'USR', 'Add workgroup'),
                        privileges: Usr.privileges.Users.admin,
                        href: '#/administration/workgroups/add',
                        itemId: 'btn-add-workgroup'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workgroups.pagingtoolbarbottom.itemsPerPage', 'USR', 'Workgroups per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
