Ext.define('Usr.view.group.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.groupList',
    itemId: 'groupList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Usr.store.Groups',
        'Usr.view.group.GroupActionMenu',
        'Uni.grid.column.Action'
    ],

    store: 'Usr.store.Groups',

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
                    dataIndex: 'name',
                    flex: 3
                },
                {
                    header: Uni.I18n.translate('general.description', 'USR', 'Description'),
                    dataIndex: 'description',
                    flex: 7
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: 'Usr.view.group.GroupActionMenu',
                    privileges: Usr.privileges.Users.admin
                }
            ]
        };

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('role.list.displayMsg', 'USR', '{0} - {1} of {2} roles'),
                displayMoreMsg: Uni.I18n.translate('role.list.displayMoreMsg', 'USR', '{0} - {1} of more than {2} roles'),
                items: [
                    {
                        text: Uni.I18n.translate('role.create', 'USR', 'Add role'),
                        action: 'createGroup',
                        itemId: 'createGroupButton',
                        href: '#/administration/roles/add',
                        privileges: Usr.privileges.Users.admin
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                limit: 10,
                itemsPerPageMsg: Uni.I18n.translate('role.list.bottom', 'USR', 'Roles per page')
            }
        ];

        this.callParent();
    }
});