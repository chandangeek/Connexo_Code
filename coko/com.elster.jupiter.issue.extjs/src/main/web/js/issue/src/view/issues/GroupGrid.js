/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.GroupGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    alias: 'widget.issues-group-grid',
    columns: [
        {
//            itemId: 'reason',
            itemId: 'description',
            dataIndex: 'description',
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
                itemsPerPage = Uni.I18n.translate('issues.grouping.reason.reasonsPerPageMsg', 'ISU', 'Reasons per page');
                me.columns[0].text = Uni.I18n.translate('general.reason', 'ISU', 'Reason');
                break;
            case 'location':
                display = Uni.I18n.translate('issues.grouping.location.displayMsg', 'ISU', '{0} - {1} of {2} locations');
                displayMore = Uni.I18n.translate('issues.groupinglocation.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} locations');
                empty = Uni.I18n.translate('issues.grouping.location.emptyMsg', 'ISU', '0 locations');
                itemsPerPage = Uni.I18n.translate('issues.grouping.location.locationsPerPageMsg', 'ISU', 'Locations per page');
                me.columns[0].text = Uni.I18n.translate('general.location', 'ISU', 'Location');
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
            pagingToolbarBottom = me.down('pagingtoolbarbottom'),
            display = '',
            displayMore = '',
            empty = '',
            itemsPerPage = '',
            descriptionColumn = me.down('#description');
//            descriptionColumn = me.down('#reason');

        me.groupingType = groupingType;

        switch(me.groupingType){
            case 'reason':
                display = Uni.I18n.translate('issues.grouping.reason.displayMsg', 'ISU', '{0} - {1} of {2} reasons');
                displayMore = Uni.I18n.translate('issues.groupingreason.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} reasons');
                empty = Uni.I18n.translate('issues.grouping.reason.emptyMsg', 'ISU', '0 reasons');
                itemsPerPage = Uni.I18n.translate('issues.grouping.reason.reasonsPerPageMsg', 'ISU', 'Reasons per page');
                descriptionColumn.setText(Uni.I18n.translate('general.reason', 'ISU', 'Reason'));
                break;
            case 'location':
                display = Uni.I18n.translate('issues.grouping.location.displayMsg', 'ISU', '{0} - {1} of {2} locations');
                displayMore = Uni.I18n.translate('issues.groupinglocation.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} locations');
                empty = Uni.I18n.translate('issues.grouping.location.emptyMsg', 'ISU', '0 locations');
                itemsPerPage = Uni.I18n.translate('issues.grouping.location.locationsPerPageMsg', 'ISU', 'Locations per page');
                descriptionColumn.setText(Uni.I18n.translate('general.location', 'ISU', 'Location'));
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