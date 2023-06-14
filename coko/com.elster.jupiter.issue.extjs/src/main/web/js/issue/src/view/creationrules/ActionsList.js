/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Isu.store.CreationRuleActionPhases',
        'Isu.store.CreationRuleActions',
        'Uni.grid.column.RemoveAction',
        'Uni.grid.column.Action',
        'Isu.view.creationrules.ActionsListMenu'
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
                header: Uni.I18n.translate('general.action', 'ISU', 'Action'),
                dataIndex: 'type',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return record.getType().get('name');
                }
            },
            {
                itemId: 'phase',
                dataIndex: 'phase',
                header: Uni.I18n.translate('issueCreationRules.actions.whenToPerform', 'ISU', 'When to perform'),
                flex: 1,
                renderer: function (value) {
                    return value ? value.title : '';
                }
            },
            {
                dataIndex: 'description',
                header: Uni.I18n.translate('general.description', 'ISU', 'Description'),
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'creation-rule-action-list-menu',
                    itemId: 'creation-rule-action-list-menu'
                }
            }
        ]
    }
});