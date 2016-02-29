Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoriesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskCommandCategoriesGrid',
    store: null,
    requires: [
        'Uni.grid.column.Action',
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
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'name',
                ascSortCls: null, // no sort indication
                descSortCls: null, // no sort indication
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                header: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                iconCls: 'uni-icon-delete',
                width: 90,
                privileges: Mdc.privileges.Communication.admin,
                items: [
                    {
                        tooltip: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                        handler: function (grid, rowIndex, colIndex, item, e, record) {
                            this.fireEvent('removeCommandCategory', grid, rowIndex, record);
                        }
                    }
                ]
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
                        itemId: 'add-command-category-action',
                        disabled: true,
                        text: Uni.I18n.translate('general.addCommandCategory', 'MDC', 'Add command category'),
                        privileges: Mdc.privileges.Communication.admin,
                        action: 'createcommunicationtaskaction',
                        href: me.router.getRoute('administration/communicationtasks/view/commandcategories/add').buildUrl()
                    }
                ],
                updateInfo: function() {
                    this.child('#displayItem').setText(
                        Uni.I18n.translatePlural('general.xCommangCategories', this.store.getCount(), 'MDC',
                            'No command categories', '{0} command category', '{0} command categories')
                    );
                }
            }
        ];
        me.callParent(arguments);
    }
});
