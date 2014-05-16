Ext.define('Usr.view.user.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.userList',
    itemId: 'userList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Usr.store.Users'
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
                    header: Uni.I18n.translate('user.name', 'USM', 'Name'),
                    dataIndex: 'authenticationName',
                    flex: 3
                },
                {
                    header: Uni.I18n.translate('user.description', 'USM', 'Description'),
                    dataIndex: 'description',
                    flex: 5
                },
                {
                    header: Uni.I18n.translate('user.domain', 'USM', 'Domain'),
                    dataIndex: 'domain',
                    flex: 2
                },
                {
                    xtype:'actioncolumn',
                    tdCls:'view',
                    header : Uni.I18n.translate('general.actions', 'USM', 'Actions'),
                    flex: 0.5,
                    items: [{
                        iconCls: 'x-uni-action-icon',
                        handler: function(grid, rowIndex, colIndex,item,e) {
                            var menu = Ext.widget('menu', {
                                    itemId: 'menuUsersList',
                                    items: [{
                                        xtype: 'menuitem',
                                        itemId: 'menuUsersListEdit',
                                        text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                                        listeners: {
                                            click: {
                                                element: 'el',
                                                fn: function(){
                                                    this.fireEvent('editUserItem',grid.getRecord(rowIndex));
                                                },
                                                scope: this
                                            }

                                        }
                                    }]
                            });

                            menu.showAt(e.getXY());
                        }
                    }]
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('user.list.top', 'USM', '{0} - {1} of {2} users')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('user.list.bottom', 'USM', 'Users per page')
            }
        ];

        this.callParent();
    }
});