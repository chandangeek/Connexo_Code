/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.readingtypes.view.GroupsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reading-type-groups-grid',
    store: 'Mtr.readingtypes.store.ReadingTypeGroups',
    router: null,
    requires: [
        'Mtr.readingtypes.view.GroupActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MTR', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('readingtypesmanagement.numberOfReadingTypes', 'MTR', '# Reading types'),
                dataIndex: 'numberOfReadingTypes',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges : Mtr.privileges.ReadingTypes.admin,
                menu: {
                    xtype: 'readingTypesGroup-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} groups'),
                displayMoreMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} groups'),
                emptyMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.emptyMsg', 'MTR', 'There are no groups to display'),
                items: [
                    {
                        xtype:'button',
                        itemId:'mtr-add-readingTypeGroup-button',
                        privileges : Mtr.privileges.ReadingTypes.admin,
                        text:Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add group')
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbarbottom.itemsPerPage', 'MTR', 'Groups per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});
