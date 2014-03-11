Ext.define('Isu.view.workspace.issues.bulk.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step1',
    title: 'Select issues',
    border: false,
    requires: [
        'Isu.view.workspace.issues.List'
    ],

    items: [
        {
            xtype: 'issues-filter',
            bodyCls: 'isu-bulk-wizard-no-border'
        },
        {
            xtype: 'issue-no-group',
            bodyCls: 'isu-bulk-wizard-no-border'
        },
        {
            xtype: 'issues-list',
            height: 285,
            selType: 'checkboxmodel',

            columns: [
                {
                    header: 'Title',
                    xtype: 'templatecolumn',
                    tpl: '{reason}<tpl if="device"> to {device.name} {device.sNumber}</tpl>',
                    flex: 2,
                    sortable: false,
                    menuDisabled: true
                },
                {
                    header: 'Due date',
                    dataIndex: 'dueDate',
                    xtype: 'datecolumn',
                    format: 'M d Y',
                    width: 140,
                    sortable: false,
                    menuDisabled: true
                },
                {
                    header: 'Status',
                    dataIndex: 'status',
                    width: 100,
                    sortable: false,
                    menuDisabled: true
                },
                {
                    header: 'Assignee',
                    xtype: 'templatecolumn',
                    tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.title}',
                    flex: 1,
                    sortable: false,
                    menuDisabled: true
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});