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
                    xtype: 'uni-actioncolumn',
                    items: [
                        {
                            text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                            itemId: 'editUser',
                            action: 'edit'
                        }
                    ]
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
                limit: 10,
                itemsPerPageMsg: Uni.I18n.translate('user.list.bottom', 'USM', 'Users per page')
            }
        ];

        this.callParent();
    }
});