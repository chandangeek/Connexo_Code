Ext.define('Isu.view.assignmentrules.List', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.layout.container.Column',
        'Ext.grid.column.Template'
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
            xtype: 'templatecolumn',
            tpl: '<tpl if="assignee.type"><span class="isu-icon-{assignee.type} isu-assignee-type-icon"></span></tpl> {assignee.name}',
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

