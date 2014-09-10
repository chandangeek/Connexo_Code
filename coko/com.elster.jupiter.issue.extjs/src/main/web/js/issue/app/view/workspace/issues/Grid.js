Ext.define('Isu.view.workspace.issues.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.view.workspace.issues.ActionMenu'
    ],
    alias: 'widget.issues-grid',
    store: 'Isu.store.Issues',
    issueType: null,
    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                itemId: 'issues-grid-title',
                header: Uni.I18n.translate('general.title.title', 'ISE', 'Title'),
                dataIndex: 'reason',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var device = record.get('device'),
                        title = value.name + (device ? ' to ' + device.name + ' ' + device.serialNumber : ''),
                        url = me.router.getRoute(me.router.currentRoute + '/view').buildUrl({id: record.getId()});

                    return '<a href="' + url + '">' + title + '</a>';
                }
            },
            {
                itemId: 'issues-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'ISE', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'issues-grid-status',
                header: Uni.I18n.translate('general.title.status', 'ISE', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'issues-grid-assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISE', 'Assignee'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'issue-action-menu',
                    itemId: 'issue-action-menu',
                    issueType: me.issueType
                }
            }
        ];

        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMsg', 'ISE', '{0} - {1} of {2} issues'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.displayMoreMsg', 'ISE', '{0} - {1} of more than {2} issues'),
                emptyMsg: Uni.I18n.translate('workspace.issues.pagingtoolbartop.emptyMsg', 'ISE', 'There are no issues to display'),
                items: [
                    '->',
                    {
                        itemId: 'bulkAction',
                        xtype: 'button',
                        text: Uni.I18n.translate('general.title.bulkActions', 'ISE', 'Bulk action'),
                        action: 'bulkchangesissues',
                        hrefTarget: '',
                        href: me.router.getRoute('workspace/' + me.issueType.toLowerCase() + '/bulk').buildUrl()
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISE', 'Issues per page')
            }
        ];

        me.callParent(arguments);
    }
});