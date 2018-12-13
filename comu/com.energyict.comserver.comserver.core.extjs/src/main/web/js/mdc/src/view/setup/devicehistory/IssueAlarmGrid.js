/**
 * Created by H251853 on 8/28/2017.
 */
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicehistory.IssueAlarmGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.view.issues.ActionMenu',
        'Isu.view.component.AssigneeColumn',
        'Isu.view.component.WorkgroupColumn',
        'Isu.privileges.Issue',
        'Uni.grid.plugin.ShowConditionalToolTip',
        'Mdc.view.setup.devicehistory.IssueAlarmActionMenu'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],
    alias: 'widget.issues-alarms-grid',
    router: null,
    store: 'Mdc.store.device.IssuesAlarms',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-id',
                header: Uni.I18n.translate('general.title.issueId', 'MDC', 'ID'),
                dataIndex: 'issueId',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({issueId: record.getId()}, {issueType: record.get('issueType').uid});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                itemId: 'issues-grid-type',
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'issueTypeName',
                flex: 1.2
            },
            {
                itemId: 'issues-grid-reason',
                header: Uni.I18n.translate('general.update', 'MDC', 'Reason'),
                dataIndex: 'reason',
                flex: 2
            },
            {
                itemId: 'issue-grid-priority',
                header: Uni.I18n.translate('general.title.priority', 'MDC', 'Priority'),
                dataIndex: 'priorityValue',
                flex: 1
            },
            {
                itemId: 'issues-grid-created',
                header: Uni.I18n.translate('general.createdOn', 'MDC', 'Created on'),
                dataIndex: 'creationDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '-';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'statusName',
                flex: 1
            },
            {
                itemId: 'issues-grid-workgroup',
                header: Uni.I18n.translate('general.workgroup', 'MDC', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                }

            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.user', 'MDC', 'User'),
                dataIndex: 'userAssigneeName',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value ? Ext.String.htmlEncode(value) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                }
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: !!Isu.privileges.Issue.adminDevice,
                renderer: function (value) {

                },
                menu: {
                    xtype: 'issues-alarms-action-menu',
                    itemId: 'issues-overview-action-menu',
                    router: me.router
                }
             }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} issues and alarms'),
                displayMoreMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} issues and alarms'),
                emptyMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.emptyMsg', 'MDC', 'There are no issues and alarms to display'),
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('workspace.general.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Issues and alarms per page')
            }
        ];
        me.callParent(arguments);
    }
});
