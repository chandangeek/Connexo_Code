/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.grid.IssuesGrid', {

    extend: 'Uni.view.grid.SelectionGrid',

    xtype: 'prefiltered-issues-grid',

    checkAllButtonPresent: true,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfIssues.selected', count, 'IMT',
            'No issues selected', '{0} issue selected', '{0} issues selected'
        );
    },

    isGridDataLoaded: false,

    columns: {
        items: [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.issue', 'IMT', 'Issue'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'issues-grid-type',
                header: Uni.I18n.translate('general.type', 'IMT', 'Type'),
                dataIndex: 'issueType_name',
                flex: 1.2
            },
            {
                itemId: 'issues-grid-priority',
                header: Uni.I18n.translate('general.priority', 'IMT', 'Priority'),
                dataIndex: 'priority',
                flex: 1
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'IMT', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-workgroup-assignee',
                header: Uni.I18n.translate('general.workgroup', 'IMT', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                }
            },
            {
                itemId: 'issues-grid-user-assignee',
                header: Uni.I18n.translate('general.user', 'IMT', 'User'),
                dataIndex: 'userAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'IMT', 'Unassigned');
                }
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
    },

    onClickCheckAllButton: function () {
        var me = this,
            collectionOfIssues = me.getStore().data.getArray()[0],
            selectionModel = me.getSelectionModel();

        Ext.suspendLayouts();
        for (var index = 0; index < collectionOfIssues.length; index++) {
            selectionModel.doSelect(index, true);
        }
        Ext.resumeLayouts(true);
    }
});