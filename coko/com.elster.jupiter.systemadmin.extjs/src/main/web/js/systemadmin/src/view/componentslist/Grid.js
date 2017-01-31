/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.view.componentslist.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.components-list',
    requires: [
        'Ext.ux.exporter.ExporterButton',
        'Ext.grid.plugin.BufferedRenderer',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    store: 'Sam.store.SystemComponents',
    maxHeight: 450,
    plugins: ['bufferedrenderer', 'showConditionalToolTip'],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.id', 'SAM', 'Id'),
                dataIndex: 'bundleId'
            },
            {
                header: Uni.I18n.translate('general.application', 'SAM', 'Application'),
                dataIndex: 'application',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.bundleType', 'SAM', 'Bundle type'),
                dataIndex: 'bundleType',
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.name', 'SAM', 'Name'),
                dataIndex: 'name',
                flex: 4
            },
            {
                header: Uni.I18n.translate('general.version', 'SAM', 'Version'),
                dataIndex: 'version',
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.status', 'SAM', 'Status'),
                dataIndex: 'status',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'toolbar',
                itemId: 'components-list-top-toolbar',
                items: [
                    '->',
                    {
                        xtype: 'exporterbutton',
                        itemId: 'components-exporter-button',
                        ui: 'icon',
                        iconCls: 'icon-file-download',
                        text: '',
                        component: me
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});