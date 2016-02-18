Ext.define('Mdc.view.setup.comtasks.ComtaskActionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskActionsGrid',
    store: null,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionActionMenu',
        'Uni.view.toolbar.PagingTop'
    ],
    router: null,
    forceFit: true,
    autoScroll: false,
    enableColumnHide: false,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                text: Uni.I18n.translate('communicationtasks.commands.category', 'MDC', 'Category'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'category',
                ascSortCls: null, // no sort indication
                descSortCls: null, // no sort indication
                flex: 1
            },
            {
                text: Uni.I18n.translate('communicationtasks.commands.action', 'MDC', 'Action'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'action',
                flex: 1
            },
            {
                itemId: 'action',
                sortable: false,
                menuDisabled: true,
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Communication.admin,
                items: 'Mdc.view.setup.comtasks.ComtaskActionActionMenu',
                align: 'left'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                border: false,
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-communication-task-action',
                        disabled: true,
                        text: Uni.I18n.translate('general.addAction', 'MDC', 'Add action'),
                        privileges: Mdc.privileges.Communication.admin,
                        action: 'createcommunicationtaskaction',
                        href: me.router.getRoute('administration/communicationtasks/view/actions/add').buildUrl()
                    }
                ],
                updateInfo: function () {
                    ; // Don't show a message like "1 - 6 of 6 actions" in this case (quite redundant)
                }
            }
        ];
        me.callParent(arguments);
    }
});
