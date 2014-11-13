Ext.define('Isu.view.assignmentrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template',
        'Isu.view.component.AssigneeColumn'
    ],
    alias: 'widget.issues-assignment-rules-list',
    store: 'Isu.store.AssignmentRules',
    height: 285,

    columns: [
        {
            header: 'Description',
            dataIndex: 'description',
            flex: 1
        },
        {
            header: 'Assign to',
            xtype: 'isu-assignee-column',
            dataIndex: 'assignee',
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                isFullTotalCount: true,
                displayMsg: Uni.I18n.translate('assignmentrules.list.pagingtoolbartop.displayMsg', 'ISU', '{2} rules'),
                emptyMsg: Uni.I18n.translate('assignmentrules.list.pagingtoolbartop.emptyMsg', 'ISU', 'There are no rules to display')
            }
        ];

        me.callParent(arguments);
    }
});

