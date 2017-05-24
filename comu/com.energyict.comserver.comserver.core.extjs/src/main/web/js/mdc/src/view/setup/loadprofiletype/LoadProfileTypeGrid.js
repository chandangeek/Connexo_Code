/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.loadProfileTypeGrid',
    itemId: 'loadProfileTypeGrid',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu',
        'Uni.grid.column.Action',
        'Uni.grid.column.Obis',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.Intervals'
    ],
    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.name','MDC','Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'obis-column',
                dataIndex: 'obisCode'
            },
            {
                header: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                dataIndex: 'timeDuration',
                renderer: function (value) {
                    var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                    return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                itemId: 'load-profile-type-action-menu-column',
                menu: {xtype: 'load-profile-type-action-menu'}
            }
        ]
    },
    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} load profile types'),
            displayMoreMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} load profile types'),
            emptyMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbartop.emptyMsg', 'MDC', 'There are no load profile types to display'),
            items: [
                {
                    text: Uni.I18n.translate('loadProfileTypes.add', 'MDC', 'Add load profile type'),
                    itemId: 'addLoadProfileType',
                    xtype: 'button',
                    action: 'addloadprofiletypeaction',
                    hrefTarget: '',
                    href: '#/administration/loadprofiletypes/add'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('loadProfileTypes.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Load profile types per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});
