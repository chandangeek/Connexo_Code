/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.DevicesOfDeviceGroupGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'devicesOfDeviceGroupGrid',
    itemId: 'allDevicesOfDeviceGroupGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DevicesOfDeviceGroup',
        'Uni.store.search.Fields',
        'Uni.view.search.ColumnPicker',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }],

    selModel: {
        mode: 'SINGLE'
    },
    store: 'Uni.store.search.Results',
    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    initComponent: function () {
        var me = this,
            service = me.getService(),
            searchFields = Ext.getStore('Uni.store.search.Fields');

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} devices'),
                displayMoreMsg: Uni.I18n.translate('devices.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} devices'),
                emptyMsg: Uni.I18n.translate('devices.pagingtoolbartop.emptyMsg', 'MDC', 'There are no devices to display'),
                items: [
                    {
                        text: Uni.I18n.translate('deviceGroup.count', 'MDC', 'Count'),
                        privileges:Mdc.privileges.DeviceGroup.adminDeviceGroup,
                        itemId: 'countButton',
                        xtype: 'button',
                        action: 'countDevicesOfGroup'
                    },
                    {
                        xtype: 'uni-search-column-picker',
                        itemId: 'column-picker',
                        grid: me
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('devices.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Devices per page'),
                dock: 'bottom',
                deferLoading: true,
                pageSizeStore: Ext.create('Ext.data.Store', {
                    fields: ['value'],
                    data: [
                        {value: '10'},
                        {value: '20'},
                        {value: '50'},
                        {value: '100'},
                        {value: '200'},
                        {value: '1000'}
                    ]
                })
            }
        ];

        var storeListeners = searchFields.on('load', function (store, items) {
            me.down('pagingtoolbartop').resetPaging();
            me.down('pagingtoolbarbottom').resetPaging();
            me.down('uni-search-column-picker').setColumns(items.map(function (field) {
                return service.createColumnDefinitionFromModel(field)
            }));
        }, me, {
            destroyable: true
        });

        me.callParent(arguments);
        me.on('destroy', function(){
            storeListeners.destroy();
        });
    }
});



