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
        var me = this,
            display = '',
            displayMore = '',
            empty = '',
            itemsPerPage = '';


        switch(me.groupingType){
            case 'reason':
                display = Uni.I18n.translate('issues.grouping.reason.displayMsg', 'ISU', '{0} - {1} of {2} reasons');
                displayMore = Uni.I18n.translate('issues.groupingreason.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} reasons');
                empty = Uni.I18n.translate('issues.grouping.reason.emptyMsg', 'ISU', '0 reasons');
                itemsPerPage =  Uni.I18n.translate('issues.grouping.reason.itemsPerPageMsg', 'ISU', 'reasons per page');
                break;
        }

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: display,
                displayMoreMsg: displayMore,
                emptyMsg: empty
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: me.store,
                deferLoading: true,
                itemsPerPageMsg: itemsPerPage,
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

        switch(me.groupingType){
            case 'reason':
                display = Uni.I18n.translate('issues.grouping.reason.displayMsg', 'ISU', '{0} - {1} of {2} reasons');
                displayMore = Uni.I18n.translate('issues.groupingreason.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} reasons');
                empty = Uni.I18n.translate('issues.grouping.reason.emptyMsg', 'ISU', '0 reasons');
                itemsPerPage =  Uni.I18n.translate('issues.grouping.reason.itemsPerPageMsg', 'ISU', 'reasons per page');
                break;
        }

        if (!Ext.isEmpty(pagingToolbarTop)) {
            pagingToolbarTop.displayMsg = display;
            pagingToolbarTop.displayMoreMsg = displayMore;
            pagingToolbarTop.emptyMsg = empty;
            pagingToolbarTop.updateInfo();
        }

        if (!Ext.isEmpty(pagingToolbarBottom)) {
            pagingToolbarBottom.down('tbtext').setText(itemsPerPage);
        }
    }
});