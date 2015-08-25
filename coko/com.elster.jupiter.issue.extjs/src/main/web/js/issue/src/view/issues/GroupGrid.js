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
            text: Uni.I18n.translate('general.reason', 'ISU', 'Reason'),
            dataIndex: 'reason',
            flex: 1
        },
        {
            itemId: 'issues_num',
            text: Uni.I18n.translate('workspace.issues.title', 'ISU', 'Issues'),
            dataIndex: 'number',
            width: 100
        }
    ],
    groupingType: 'default',

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.displayMsg', 'ISU', '{0} - {1} of {2} items'),
                displayMoreMsg: Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} items'),
                emptyMsg: Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.emptyMsg', 'ISU', '0 items')
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.itemsPerPageMsg', 'ISU', 'Items per page'),
                isSecondPagination: true
            }
        ];

        me.callParent(arguments);
    },

    updateGroupingType: function (groupingType) {
        var me = this,
            pagingToolbarTop = me.down('pagingtoolbartop'),
            pagingToolbarBottom = me.down('pagingtoolbarbottom');

        me.groupingType = groupingType;
        if (!Ext.isEmpty(pagingToolbarTop)) {
            pagingToolbarTop.displayMsg = Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.displayMsg', 'ISU', '{0} - {1} of {2} items');
            pagingToolbarTop.displayMoreMsg = Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} items');
            pagingToolbarTop.emptyMsg = Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.emptyMsg', 'ISU', '0 items');
            pagingToolbarTop.updateInfo();
        }

        if (!Ext.isEmpty(pagingToolbarBottom)) {
            pagingToolbarBottom.down('tbtext').setText(Uni.I18n.translate('issues.grouping.'+ me.groupingType + '.itemsPerPageMsg', 'ISU', 'Items per page'));
        }
    }
});