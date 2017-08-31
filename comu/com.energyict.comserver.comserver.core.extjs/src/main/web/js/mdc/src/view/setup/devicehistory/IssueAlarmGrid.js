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
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],
    alias: 'widget.issues-alarms-grid',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-type',
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'issueType_name',
                flex: 1.2
            },
            {
                itemId: 'issues-grid-update',
                header: Uni.I18n.translate('general.update', 'MDC', 'Update'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'issues-grid-created',
                header: Uni.I18n.translate('general.created', 'MDC', 'Created On'),
                dataIndex: 'creationDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '-';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status_name',
                flex: 1
            },
            {
                itemId: 'issues-grid-workgroup',
                header: Uni.I18n.translate('general.workgroup', 'MDC', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                }
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.user', 'MDC', 'User'),
                dataIndex: 'userAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    return value.name ? Ext.String.htmlEncode(value.name) : Uni.I18n.translate('general.unassigned', 'MDC', 'Unassigned');
                }
            }
            /* {
             itemId: 'action',
             xtype: 'uni-actioncolumn',
             width: 120,
             privileges: !!Isu.privileges.Issue.adminDevice,
             menu: {
             xtype: 'issues-action-menu',
             itemId: 'issues-overview-action-menu',
             router: me.router
             }
             }*/
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} issues/alarms'),
                displayMoreMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} issues/alarms'),
                emptyMsg: Uni.I18n.translate('workspace.general.pagingtoolbartop.emptyMsg', 'MDC', 'There are no issues/alarms to display'),
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.general.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Issues and alarms per page')
            }
        ];
        me.callParent(arguments);
    }
});
