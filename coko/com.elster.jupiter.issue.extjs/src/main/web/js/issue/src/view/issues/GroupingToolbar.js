/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.GroupingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.issues-grouping-toolbar',
    title: Uni.I18n.translate('general.group', 'ISU', 'Group'),
    showClearButton: false,
    store: 'Isu.store.IssueGrouping',
    groupingType: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'combobox',
            itemId: 'issues-grouping-toolbar-combo',
            store: me.store,
            editable: true,
            forceSelection: true,
            value: me.groupingType,
            queryMode: 'local',
            displayField: 'value',
            valueField: 'id'
        };

        me.callParent(arguments);
    }
});
