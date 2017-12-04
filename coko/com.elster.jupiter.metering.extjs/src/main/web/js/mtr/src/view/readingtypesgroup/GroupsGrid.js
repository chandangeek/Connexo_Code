/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.GroupsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reading-type-groups-grid',
    store: 'Mtr.store.readingtypesgroup.ReadingTypeGroups',
    router: null,
    requires: [
        'Mtr.view.readingtypesgroup.GroupActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MTR', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/readingtypegroups/view').buildUrl({aliasName: encodeURIComponent(value)});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('readingtypesmanagement.numberOfReadingTypes', 'MTR', 'Active reading types'),
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

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} groups'),
                displayMoreMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} groups'),
                emptyMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.emptyMsg', 'MTR', 'There are no groups to display'),
                items: [
                    {
                        xtype:'button',
                        itemId:'mtr-add-readingTypeGroup-button',
                        privileges : Mtr.privileges.ReadingTypes.admin,
                        text:Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add reading type group')
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                itemsPerPageMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbarbottom.itemsPerPage', 'MTR', 'Groups per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        this.callParent();
    }
});
