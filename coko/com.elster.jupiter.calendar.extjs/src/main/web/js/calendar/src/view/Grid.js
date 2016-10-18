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
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.category', 'CAL', 'Category'),
                dataIndex: 'category',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.Description', 'CAL', 'Description'),
                dataIndex: 'description',
                flex: 5
            },
            {
                header: Uni.I18n.translate('general.status', 'CAL', 'Status'),
                dataIndex: 'status',
                flex: 2
            },
            {
                xtype: 'uni-actioncolumn',
                //privileges: Scs.privileges.ServiceCall.admin,
                menu: {
                    xtype: 'tou-action-menu',
                    //itemId: me.menuItemId
                },
                flex: 0.5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('calendar.pagingtoolbartop.displayMsg', 'CAL', '{0} calendars'),
                emptyMsg: Uni.I18n.translate('calendar.pagingtoolbartop.emptyMsg', 'CAL', 'There are no calendars to display'),
                usesExactCount: true,
                noBottomPaging: true
            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('calendar.pagingtoolbarbottom.displayMsg', 'MDC', 'Calendars per page'),
                store: me.store,
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    },
});