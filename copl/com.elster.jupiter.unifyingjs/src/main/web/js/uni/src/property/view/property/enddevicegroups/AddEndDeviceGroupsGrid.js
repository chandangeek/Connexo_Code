/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
 
 Ext.define('Uni.property.view.property.enddevicegroups.AddEndDeviceGroupsGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    alias: 'widget.uni-device-groups-selection-grid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
		'Uni.property.model.PropertyEndDeviceGroup',
		'Uni.property.store.PropertyEndDeviceGroups'
    ],

    selType: 'checkboxmodel',
    
    selModel: {
        mode: 'MULTI',
        checkOnly: true,
    },
    
    store: 'Uni.property.store.PropertyEndDeviceGroups',

    initComponent: function () {
        var me = this;
        
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'UNI', 'Type'),
                dataIndex: 'dynamic',
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.dynamic', 'UNI', 'Dynamic')
                    } else {
                        return Uni.I18n.translate('general.static', 'UNI', 'Static')
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
                displayMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMsg', 'UNI', '{0} - {1} of {2} device groups'),
                displayMoreMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.displayMoreMsg', 'UNI', '{0} - {1} of more than {2} device groups'),
                emptyMsg: Uni.I18n.translate('deviceGroup.pagingtoolbartop.emptyMsg', 'UNI', 'There are no device groups to display'),
                items: []
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                isSecondPagination: true,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceGroup.pagingtoolbarbottom.itemsPerPage', 'UNI', 'Device groups per page')
            }
        ];

        me.callParent();
    }
});