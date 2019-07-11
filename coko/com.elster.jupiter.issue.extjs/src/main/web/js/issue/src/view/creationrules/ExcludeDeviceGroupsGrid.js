/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.ExcludeDeviceGroupsGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    alias: 'widget.isu-device-groups-selection-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Isu.model.FilteredDeviceGroup',
        'Isu.store.FilteredDeviceGroups'
    ],

    selType: 'checkboxmodel',
    
    selModel: {
        mode: 'MULTI',
        checkOnly: true,
    },
    
    store: 'Isu.store.FilteredDeviceGroups',

    initComponent: function () {
        var me = this;
        
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'ISU', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'ISU', 'Type'),
                dataIndex: 'dynamic',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.dynamic', 'ISU', 'Dynamic')
                    } else {
                        return Uni.I18n.translate('general.static', 'ISU', 'Static')
                    }
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMsg', 'ISU', '{0} - {1} of {2} device groups'),
                displayMoreMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMoreMsg', 'ISU', '{0} - {1} of more than {2} device groups'),
                emptyMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.emptyMsg', 'ISU', 'There are no device groups to display'),
                items: []
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                isSecondPagination: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceGroup.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device groups per page')
            }
        ];

        me.callParent();
    }
});