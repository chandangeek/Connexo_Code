/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Dal.store.CreationRuleActionPhases',
        'Dal.store.CreationRuleActions',
        'Uni.grid.column.RemoveAction'
    ],
    viewConfig: {
        markDirty: false
    },
    alias: 'widget.alarms-creation-rules-actions-list',
    store: 'ext-empty-store',
    enableColumnHide: false,
    columns: {
        items: [
            {
                itemId: 'description',
                header: Uni.I18n.translate('general.action', 'DAL', 'Action'),
                dataIndex: 'type',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getType().get('name');
                }
            },
            {
                itemId: 'phase',
                dataIndex: 'phase',
                header: Uni.I18n.translate('alarmCreationRules.actions.whenToPerform', 'DAL', 'When to perform'),
                flex: 1,
                renderer: function (value) {
                    return value ? value.title : '';
                }
            },
            {
                dataIndex: 'description',
                header: Uni.I18n.translate('general.description', 'DAL', 'Description'),
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