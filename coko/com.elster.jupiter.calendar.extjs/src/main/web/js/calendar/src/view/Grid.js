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
                header: Uni.I18n.translate('general.Description', 'CAL', 'Description'),
                dataIndex: 'description',
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                //privileges: Scs.privileges.ServiceCall.admin,
                menu: {
                    xtype: 'tou-action-menu',
                    //itemId: me.menuItemId
                },
                flex: 0.7
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('calendars.timeOfUse.pagingtoolbartop.displayMsg', 'CAL', '{0} time of use calendars'),
                emptyMsg: Uni.I18n.translate('calendars.timeOfUse.pagingtoolbartop.emptyMsg', 'CAL', 'There are no time of use calendars to display'),
            }
        ];

        me.callParent(arguments);
    },
});