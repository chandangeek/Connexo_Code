Ext.define('Isu.view.workspace.issues.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-list',
    store: 'Isu.store.Issues',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Title',
                header: Uni.I18n.translate('general.title.title', 'ISE', 'Title'),
                xtype: 'templatecolumn',
                tpl: '<a href="#/workspace/datacollection/issues/{id}">{title}</a>',
                flex: 2
            },
            {
                itemId: 'dueDate',
                header: Uni.I18n.translate('general.title.dueDate', 'ISE', 'Due date'),
                dataIndex: 'dueDate',
                xtype: 'datecolumn',
                format: 'M d Y',
                width: 140
            },
            {
                itemId: 'status',
                header: Uni.I18n.translate('general.title.status', 'ISE', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'assignee',
                header: Uni.I18n.translate('general.title.assignee', 'ISE', 'Assignee'),
                xtype: 'templatecolumn',
                tpl: '<tpl if="assignee_type"><span class="isu-icon-{assignee_type} isu-assignee-type-icon"></span></tpl> {assignee_name}',
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Isu.view.workspace.issues.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
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
                    href: '#/workspace/datacollection/bulkaction'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('workspace.issues.pagingtoolbarbottom.itemsPerPage', 'ISE', 'Issues per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});