/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.DeviceGroupsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.grid.column.RemoveAction'
    ],
    viewConfig: {
        markDirty: false
    },
    alias: 'widget.issues-creation-rules-excl-device-groups-list',
    store: 'ext-empty-store',
    enableColumnHide: false,
    columns: {
        items: [
            {
                itemId: 'description',
                header: Uni.I18n.translate('general.name', 'ISU', 'Name'),
                dataIndex: 'deviceGroupName',
                flex: 1
            },
            {
                itemId: 'isDynamic',
                header: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                dataIndex: 'isGroupDynamic',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.dynamic', 'ISU', 'Dynamic')
                    } else {
                        return Uni.I18n.translate('general.static', 'ISU', 'Static')
                    }
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn-remove',
                handler: function (grid, rowIndex) {
                    var store = grid.getStore(),
                        gridPanel = grid.up(),
                        emptyMsg = gridPanel.up().down('displayfield');

                    store.removeAt(rowIndex);
                    if (!store.getCount()) {
                        Ext.suspendLayouts();
                        gridPanel.hide();
                        emptyMsg.show();
                        Ext.resumeLayouts(true);
                    }
                }
            }
        ]
    }
});