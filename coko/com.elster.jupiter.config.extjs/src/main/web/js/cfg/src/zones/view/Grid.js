/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.zones-grid',
    store: 'Cfg.zones.store.Zones',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.DateTime'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/zones/view').buildUrl({zoneId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.zoneType', 'CFG', 'Zone type'),
                dataIndex: 'zoneTypeName',
                renderer: function (value) {
                    return value;
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'zones-action-menu',
                    itemId: 'zones-action-menu'
                }
            }
        ];

        me.dockedItems = [
           /* {
                xtype: 'zones-overview-filter',
                itemId: 'zones-overview-filter'
            },*/
            {
                xtype: 'pagingtoolbartop',
                itemId: 'zones-grid-paging-toolbar-top',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('zones.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} zones'),
                displayMoreMsg: Uni.I18n.translate('zones.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} zones'),
                emptyMsg: Uni.I18n.translate('zones.pagingtoolbartop.emptyMsg', 'CFG', 'There are no zones to display'),
                items: [
                    {
                        itemId: 'zones-add-button',
                        text: Uni.I18n.translate('zones.addZone', 'CFG', 'Add zone'),
                        action: 'addZone',
                        privileges: Cfg.privileges.Validation.adminZones,
                    }
                ]
            },
            {
                itemId: 'zones-grid-paging-toolbar-bottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('zones.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Zones per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
