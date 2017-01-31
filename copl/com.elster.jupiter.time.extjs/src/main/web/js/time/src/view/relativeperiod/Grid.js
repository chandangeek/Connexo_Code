/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.view.relativeperiod.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.relative-periods-grid',
    store: 'Tme.store.RelativePeriods',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/relativeperiods/relativeperiod').buildUrl({periodId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                dataIndex: 'listOfCategories',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'relative-periods-action-menu',
                    itemId: 'relative-periods-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('relativePeriods.pagingtoolbartop.displayMsg', 'TME', '{0} - {1} of {2} relative periods'),
                displayMoreMsg: Uni.I18n.translate('relativePeriods.pagingtoolbartop.displayMoreMsg', 'TME', '{0} - {1} of more than {2} relative periods'),
                emptyMsg: Uni.I18n.translate('relativePeriods.pagingtoolbartop.emptyMsg', 'TME', 'There are no relative periods to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-add-relative-period',
                        privileges : Tme.privileges.Period.admin,
                        text: Uni.I18n.translate('relativeperiod.add', 'TME', 'Add relative period'),
                        href: me.router.getRoute('administration/relativeperiods').buildUrl() + '/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('relativePeriods.pagingtoolbarbottom.itemsPerPage', 'TME', 'Relative periods per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

