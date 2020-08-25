/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.ReadingTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reading-types-in-group-grid',
    itemId: 'reading-types-in-group-grid',
    store: 'Mtr.store.ReadingTypesByAlias',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mtr.store.ReadingTypesByAlias',
        'Mtr.view.ActionMenu'
    ],
    initComponent: function(){
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('readingtypesmanagement.readingTypes', 'MTR', 'Reading type'),
                dataIndex: 'fullAliasName',
                flex: 3
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
                privileges: Mtr.privileges.ReadingTypes.admin,
                menu: {
                    xtype: 'reading-types-action-menu'
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
                items:
                    [
                        {
                            xtype: 'button',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'add',
                            itemId: 'reading-types-add-action-button-disabled-fields',   // CXO-8276
                            text: Uni.I18n.translate('readingtypes.readingTypes.add', 'MTR', 'Add reading type')
                        },
                        {
                            xtype: 'button',
                            privileges: Mtr.privileges.ReadingTypes.admin,
                            action: 'bulk',
                            itemId: 'reading-types-set-bulk-action-button',
                            text: Uni.I18n.translate('general.bulk', 'MTR', 'Bulk action')
                        }
                    ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('readingtypesmanagment.pagingtoolbarbottom.itemsPerPage', 'MTR', 'Reading types per page'),
                dock: 'bottom',
                needExtendedData: true,
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }

});
