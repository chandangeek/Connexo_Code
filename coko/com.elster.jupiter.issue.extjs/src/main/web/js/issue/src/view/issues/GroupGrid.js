Ext.define('Isu.view.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-group-grid',
    columns: [
        {
            itemId: 'reason',
            text: 'Reason',
            dataIndex: 'reason',
            flex: 1
        },
        {
            itemId: 'issues_num',
            text: 'Issues',
            dataIndex: 'number',
            width: 100
        }
    ],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} items'),
                emptyMsg: '0 reasons'
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('workspace.issues.groupGrid.pagingtoolbartop.itemsPerPageMsg', 'ISU', 'Items per page')
            }
        ];

        me.callParent(arguments);
    }
});