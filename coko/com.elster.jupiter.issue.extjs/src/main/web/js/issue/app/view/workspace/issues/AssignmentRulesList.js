Ext.define('Mtr.view.workspace.issues.AssignmentRulesList', {
    extend: 'Ext.grid.Panel',
    requires: [
    ],
    store: 'Mtr.store.AssignmentRules',
    alias: 'widget.issues-assignment-rules',
    emptyText: '',
    columns: [
        {
            header: 'Priority',
            xtype: 'templatecolumn',
            tpl: '{priority}</tpl>',
            flex: 2,
            sortable: false
        },
        {
            header: 'When',
            dataIndex: 'when',
            width: 140,
            sortable: false
        },
        {
            header: 'Assign to',
            tpl: '<tpl if="assignTo.type"><span class="isu-icon-{assignee.type}"></span></tpl> {assignTo.title}',
            width: 100,
            sortable: false
        },
        {
            header: 'Status',
            xtype: 'templatecolumn',
            tpl: '<tpl>{status}</tpl>',
            flex: 1,
            sortable: false
        },
        {
            header: 'Actions',
            xtype: 'actioncolumn',
            iconCls: 'isu-action-icon',
            width: 70,
            align: 'left',
            sortable: false
        }
    ],
})

