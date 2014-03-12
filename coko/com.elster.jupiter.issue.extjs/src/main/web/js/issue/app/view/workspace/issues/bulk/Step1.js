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

            columns: {
                defaults: {
                    sortable: false,
                    menuDisabled: true
                },
                items: [
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
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="status">{status.name}</tpl>',
                        width: 100
                    },
                    {
                        header: 'Assignee',
                        xtype: 'templatecolumn',
                        tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignee.name}',
                        flex: 1
                    }
                ]
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});