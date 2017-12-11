/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reading-types-in-group-grid',
    itemId: 'reading-types-in-group-grid',
    store: 'Mtr.store.readingtypes.ReadingTypesByAlias',
    router: null,
    requires: [
        'Mtr.view.readingtypesgroup.ReadingTypesGridActionMenu',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mtr.store.readingtypes.ReadingTypesByAlias'
    ],
    initComponent: function(){
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('readingtypesmanagement.readingTypes', 'MTR', 'Reading Type'),
                dataIndex: 'fullAliasName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('readingtypesmanagment.status', 'MTR', 'Status'),
                dataIndex: 'active',
                renderer: function(value){
                    return value
                        ? Uni.I18n.translate('readingtypesmanagement.active', 'MTR', 'Active')
                        : Uni.I18n.translate('readingtypesmanagement.inactive', 'MTR', 'Inactive');
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'reading-types-grid-action-menu',
                    itemId: 'reading-types-grid-action-menu'
                }
            }
        ]

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} reading types'),
                displayMoreMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} reading types'),
                emptyMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.emptyMsg', 'MTR', 'There are no reading types to display'),
                items: [
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
