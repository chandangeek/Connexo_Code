/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.datasources.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.data-sources-grid',
    store: 'Dxp.store.DataSources',
    router: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.ReadingType',
        'Uni.util.Application'
    ],
    commonColumns: [
        {
            xtype: 'reading-type-column',
            header: Uni.I18n.translate('general.readingType', 'DES', 'Reading type'),
            dataIndex: 'readingType',
            flex: 2
        },
        {
            header: Uni.I18n.translate('general.lastExportedDate', 'DES', 'Last exported date'),
            dataIndex: 'lastExportedDate',
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeShort(value) : Uni.I18n.translate('general.neverExported', 'DES', 'Never exported');
            },
            flex: 1
        }
    ],
    columnsPerApp: {
        MultiSense: [
            {
                header: Uni.I18n.translate('general.name', 'DES', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.serialNumber', 'DES', 'Serial number'),
                dataIndex: 'serialNumber',
                flex: 1
            }
        ],
        MdmApp: [
            {
                header: Uni.I18n.translate('general.name', 'DES', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.connectionState', 'DES', 'Connection state'),
                dataIndex: 'connectionState',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.columns = Ext.Array.merge(me.columnsPerApp[Uni.util.Application.getAppName()] || [], me.commonColumns);

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.displayMsg', 'DES', '{0} - {1} of {2} data sources'),
                displayMoreMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.displayMoreMsg', 'DES', '{0} - {1} of more than {2} data sources'),
                emptyMsg: Uni.I18n.translate('dataSources.pagingtoolbartop.emptyMsg', 'DES', 'There are no data sources to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dataSources.pagingtoolbarbottom.itemsPerPage', 'DES', 'Data sources per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
