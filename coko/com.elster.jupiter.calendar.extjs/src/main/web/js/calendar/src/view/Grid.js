/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cal.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tou-grid',
    store: 'Cal.store.TimeOfUseCalendars',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Cal.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'CAL', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.category', 'CAL', 'Category'),
                dataIndex: 'category',
                flex: 2,
                renderer: function(value){
                    return value.displayName
                }
            },
            {
                header: Uni.I18n.translate('general.Description', 'CAL', 'Description'),
                dataIndex: 'description',
                flex: 5
            },
            {
                header: Uni.I18n.translate('general.status', 'CAL', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function(value){
                    return value.displayValue;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'tou-action-menu'
                    //itemId: me.menuItemId
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMoreMsg: Uni.I18n.translate('calendar.pagingtoolbartop.displayMoreMsg', 'CAL', '{0} - {1} of more than {2} calendars'),
                displayMsg: Uni.I18n.translate('calendar.pagingtoolbartop.displayMsg', 'CAL', '{0} - {1} of {2} calendars'),
                emptyMsg: Uni.I18n.translate('calendar.pagingtoolbartop.emptyMsg', 'CAL', 'There are no calendars to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('calendar.pagingtoolbarbottom.displayMsg', 'CAL', 'Calendars per page'),
                store: me.store,
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    },
});