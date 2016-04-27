Ext.define('Mdc.timeofuse.view.CalendarsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tou-calendars-grid',
    //store: 'Mdc.store.ServiceCalls',

    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Mdc.timeofuse.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Scs.privileges.ServiceCall.admin,
                menu: {
                    xtype: 'tou-devicetype-action-menu'
                },
                flex: 0.7
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                //store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('timeofuse.pagingtoolbartop.displayMsg', 'MDC', '{0} Time of use calendars'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('tou.addTouCalendars', 'MDC', 'Add time of use calendars'),
                        itemId: 'add-tou-calendars-btn'
                    }
                ]

            }
        ];

        me.callParent(arguments);
    }
});