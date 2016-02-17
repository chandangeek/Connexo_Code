Ext.define('Isu.view.issues.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.view.issues.ActionMenu',
        'Isu.view.component.AssigneeColumn',
        'Isu.privileges.Issue'
    ],
    alias: 'widget.issues-grid',
    dataCollectionActivated: false,
    dataValidationActivated: false,
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.issue', 'ISU', 'Issue'),
                dataIndex: 'title',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({issueId: record.getId()});
                    switch (record.get('issueType').uid) {
                        case 'datacollection':
                            if (me.dataCollectionActivated) {
                                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            } else {
                                return Ext.String.htmlEncode(value);
                            }
                            break;
                        case 'datavalidation':
                            if (me.dataValidationActivated) {
                                return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            } else {
                                return Ext.String.htmlEncode(value);
                            }
                            break;
                    }
                }
            },
            {
                itemId: 'issues-grid-type',
                header: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                dataIndex: 'issueType_name',
                width: 140
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-modification-date',
                header: Uni.I18n.translate('general.title.modificationDate', 'ISU', 'Modification date'),
                dataIndex: 'modTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.status', 'ISU', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.assignee', 'ISU', 'Assignee'),
                xtype: 'isu-assignee-column',
                dataIndex: 'assignee',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                privileges: !!Isu.privileges.Issue.adminDevice,
                menu: {
                    xtype: 'issues-action-menu',
                    dataCollectionActivated: me.dataCollectionActivated,
                    dataValidationActivated: me.dataValidationActivated,
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
                displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} issues'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} issues'),
                emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ISU', 'There are no issues to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'issues-bulk-action',
                        text: Uni.I18n.translate('general.title.bulkActions', 'ISU', 'Bulk action'),
                        privileges: Isu.privileges.Issue.commentOrAssing,
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
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISU', 'Issues per page')
            }
        ];
        me.callParent(arguments);
    }
});