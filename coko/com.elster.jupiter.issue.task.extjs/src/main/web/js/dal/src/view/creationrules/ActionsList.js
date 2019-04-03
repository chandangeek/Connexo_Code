/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.creationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Itk.store.CreationRuleActionPhases',
        'Itk.store.CreationRuleActions',
        'Uni.grid.column.RemoveAction'
    ],
    viewConfig: {
        markDirty: false
    },
    alias: 'widget.issues-creation-rules-actions-list',
    store: 'ext-empty-store',
    enableColumnHide: false,
    columns: {
        items: [
            {
                itemId: 'description',
                header: Uni.I18n.translate('general.action', 'ITK', 'Action'),
                dataIndex: 'type',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getType().get('name');
                }
            },
            {
                itemId: 'phase',
                dataIndex: 'phase',
                header: Uni.I18n.translate('issueCreationRules.actions.whenToPerform', 'ITK', 'When to perform'),
                flex: 1,
                renderer: function (value) {
                    return value ? value.title : '';
                }
            },
            {
                dataIndex: 'description',
                header: Uni.I18n.translate('general.description', 'ITK', 'Description'),
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