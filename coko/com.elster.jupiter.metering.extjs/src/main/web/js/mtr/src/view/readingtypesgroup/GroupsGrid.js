/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.GroupsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.reading-type-groups-grid',
    store: 'Mtr.store.readingtypesgroup.ReadingTypeGroups',
    router: null,
    requires: [
        'Mtr.view.readingtypesgroup.ReadingTypesGridActionMenu',
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
                    //var url = me.router.getRoute('administration/readingtypegroups/view').buildUrl({aliasName: encodeURIComponent(value)}); // lori set
                    var url = me.router.getRoute('administration/readingtypes1/view').buildUrl({aliasName: encodeURIComponent(value)});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('readingtypesmanagement.numberOfReadingTypes', 'MTR', 'Active reading types'),
                dataIndex: 'numberOfReadingTypes',
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
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                // displayMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} groups'),
                // displayMoreMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} groups'),
                // emptyMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.emptyMsg', 'MTR', 'There are no groups to display'),
                displayMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMsg', 'MTR', '{0} - {1} of {2} reading type sets'),
                displayMoreMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.displayMoreMsg', 'MTR', '{0} - {1} of more than {2} reading type sets'),
                emptyMsg: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.emptyMsg', 'MTR', 'There are no reading type sets to display'),
                items: [
                    {
                        xtype:'button',
                        itemId:'mtr-add-readingTypeGroup-button',
                        privileges : Mtr.privileges.ReadingTypes.admin,
                        text: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add reading type')
                        // text:Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add reading type group') // lori set
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
