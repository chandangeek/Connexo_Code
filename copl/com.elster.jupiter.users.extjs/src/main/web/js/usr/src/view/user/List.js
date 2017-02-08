/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.userList',
    itemId: 'userList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Usr.store.Users',
        'Usr.view.user.UserActionMenu',
        'Uni.grid.column.Action'
    ],

    store: 'Usr.store.Users',

    initComponent: function () {
        this.columns = {
            defaults: {
                flex: 1,
                sortable: false,
                hideable: false,
                fixed: true
            },
            items: [
                {
                    header: Uni.I18n.translate('general.name', 'USR', 'Name'),
                    dataIndex: 'authenticationName',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
                    dataIndex: 'description',
                    flex: 5
                },
                {
                    header: Uni.I18n.translate('user.userdirectory', 'USR', 'User directory'),
                    dataIndex: 'domain',
                    flex: 2
                },
                {
                    header: Uni.I18n.translate('user.status', 'USR', 'Status'),
                    dataIndex: 'statusDisplay',
                    flex: 2
                },
                {
                    xtype: 'uni-actioncolumn',
                    privileges: Usr.privileges.Users.adminUsers,

                    menu: {
                        xtype: 'user-action-menu',
                        itemId: 'user-action-menu'
                    }
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('user.list.top', 'USR', '{0} - {1} of {2} users')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                limit: 10,
                itemsPerPageMsg: Uni.I18n.translate('user.list.bottom', 'USR', 'Users per page')
            }
        ];

        this.callParent();
    }
});