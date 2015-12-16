Ext.define('Mdc.view.setup.logbooktype.LogbookTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.logbookTypeGrid',
    overflowY: 'auto',
    itemId: 'logbookTypeGrid',
    store: 'Mdc.store.LogbookTypes',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.LogbookTypes',
        'Mdc.view.setup.logbooktype.LogbookTypeActionMenu',
        'Uni.grid.column.Obis'
    ],

    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3

            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode',
                flex: 2
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.MasterData.admin,
                items: 'Mdc.view.setup.logbooktype.LogbookTypeActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} logbook types'),
                displayMoreMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} logbook types'),
                emptyMsg: Uni.I18n.translate('logbooktype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no logbook types to display'),
                items: [
                    {
                        text: Uni.I18n.translate('logbooktype.add', 'MDC', 'Add logbook type'),
                        privileges: Mdc.privileges.MasterData.admin,
                        itemId: 'createLogbookType',
                        xtype: 'button',
                        action: 'createLogbookType'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('logbooktype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Logbook types per page')
            }
        ];

        me.callParent();
    }
});
