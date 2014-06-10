Ext.define('Usr.view.group.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.groupList',
    itemId: 'groupList',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Usr.store.Groups'
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
                    header: Uni.I18n.translate('group.name', 'USM', 'Name'),
                    dataIndex: 'name',
                    flex: 3
                },
                {
                    header: Uni.I18n.translate('group.description', 'USM', 'Description'),
                    dataIndex: 'description',
                    flex: 7
                },
                {
                    xtype: 'uni-actioncolumn',
                    items: [
                        {
                            text: Uni.I18n.translate('general.edit', 'USM', 'Edit'),
                            itemId: 'editGroup',
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
                displayMsg: Uni.I18n.translate('group.list.top', 'USM', '{0} - {1} of {2} roles'),
                items: [
                    '->',
                    {
                        text: Uni.I18n.translate('group.add', 'USM', 'Add role'),
                        action: 'createGroup'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                limit: 10,
                itemsPerPageMsg: Uni.I18n.translate('group.list.bottom', 'USM', 'Roles per page')
            }
        ];

        this.callParent();
    }
});