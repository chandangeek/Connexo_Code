Ext.define('Mtr.view.workspace.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',
    requires: [
        'Mtr.view.workspace.issues.List'
    ],

    items: [
        {
            xtype: 'issues-filter'
        },
        {
            xtype: 'issue-no-group'
        },
        {
            xtype: 'issues-list',
            height: 285,
            dockedItems: [],
            selType: 'checkboxmodel',

            columns: [
                {
                    header: 'Title',
                    xtype: 'templatecolumn',
                    tpl: '{reason}<tpl if="device"> to {device.name} {device.sNumber}</tpl>',
                    flex: 2
                },
                {
                    header: 'Due date',
                    dataIndex: 'dueDate',
                    xtype: 'datecolumn',
                    format: 'M d Y',
                    width: 140
                },
                {
                    header: 'Status',
                    dataIndex: 'status',
                    width: 100
                },
                {
                    header: 'Assignee',
                    xtype: 'templatecolumn',
                    tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.title}',
                    flex: 1
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});