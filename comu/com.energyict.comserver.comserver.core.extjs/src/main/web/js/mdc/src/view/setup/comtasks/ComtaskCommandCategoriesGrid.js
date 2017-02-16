/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskCommandCategoriesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskCommandCategoriesGrid',
    store: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.grid.column.RemoveAction'
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
                xtype: 'uni-actioncolumn-remove',
                privileges: Mdc.privileges.Communication.admin,
                handler: function (grid, rowIndex, colIndex, item, e, record) {
                    this.fireEvent('removeCommandCategory', grid, rowIndex, record);
                }
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
                        text: Uni.I18n.translate('comtask.commandCategories.add', 'MDC', 'Add command categories'),
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
