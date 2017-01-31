/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.metering-reading-types-grid',
    itemId: 'metering-reading-types-grid',
    store: 'Mtr.readingtypes.store.ReadingTypes',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MTR', 'Name'),
                dataIndex: 'fullAliasName',
                flex: 3
            },            
            {
                header: Uni.I18n.translate('readingtypesmanagment.status', 'MTR', 'Status'),
                dataIndex: 'active',
                renderer: function(value){
                    return value
                        ? Uni.I18n.translate('readingtypesmanagment.active', 'MTR', 'Active')
                        : Uni.I18n.translate('readingtypesmanagment.inactive', 'MTR', 'Inactive');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges : Mtr.privileges.ReadingTypes.admin,
                menu: {
                    xtype: 'reading-types-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} reading types'),
                displayMoreMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} reading types'),
                emptyMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.emptyMsg', 'MTR', 'There are no reading types to display'),
                items: [{
                    xtype:'button',
                    itemId:'add-reading-type-button',
                    privileges : Mtr.privileges.ReadingTypes.admin,
                    text:Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.addButton', 'MTR', 'Add reading types')
                },
                    {
                        xtype: 'button',
                        privileges : Mtr.privileges.ReadingTypes.admin,
                        action: 'bulk',
                        itemId: 'reading-types-bulk-action-button',
                        text: Uni.I18n.translate('general.bulkAction', 'MTR', 'Bulk action')
                    }]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbarbottom.itemsPerPage', 'MTR', 'Reading types per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});