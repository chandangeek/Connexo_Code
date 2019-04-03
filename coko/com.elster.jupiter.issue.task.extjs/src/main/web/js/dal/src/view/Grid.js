/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Itk.privileges.Issue',
        'Itk.view.ActionMenu'
    ],
    alias: 'widget.issues-grid',
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.issue', 'ITK', 'Issue'),
                dataIndex: 'title',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({issueId: record.getId()});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                itemId: 'issues-grid-priority',
                header: Uni.I18n.translate('general.priority', 'ITK', 'Priority'),
                dataIndex: 'priority',
                flex: 1
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ITK', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'ITK', 'Status'),
                dataIndex: 'statusName',
                flex: 1
            },
            {
                itemId: 'issues-grid-cleared',
                header: Uni.I18n.translate('general.cleared', 'ITK', 'Cleared'),
                dataIndex: 'cleared',
                flex: 1
            },
            {
                itemId: 'issues-grid-workgroup',
                header: Uni.I18n.translate('general.workgroup', 'ITK', 'Workgroup'),
                dataIndex: 'workgroup',
                flex: 1
            },
            {
                itemId: 'issues-grid-user',
                header: Uni.I18n.translate('general.user', 'ITK', 'User'),
                dataIndex: 'user',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: !!Itk.privileges.Issue.adminDevice,
                menu: {
                    xtype: 'issues-action-menu',
                    itemId: 'issue-overview-action-menu',
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
                displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ITK', '{0} - {1} of {2} issues'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ITK', '{0} - {1} of more than {2} issues'),
                emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ITK', 'There are no issues to display')
                ,
             items: [
                 {
                     xtype: 'button',
                     itemId: 'issues-bulk-action',
                     text: Uni.I18n.translate('general.title.bulkActions', 'ITK', 'Bulk action'),
                     privileges: Itk.privileges.Issue.closeOrAssing,
                     action: 'issuesBulkAction',
                     handler: function () {
                         me.router.getRoute(me.router.currentRoute + '/bulkaction').forward(me.router.arguments, Uni.util.QueryString.getQueryStringValues(false));
                     }
                 }
             ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ITK', 'Issues per page')
            }
        ];
        me.callParent(arguments);
    }
});