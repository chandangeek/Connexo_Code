/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.DevicesOfZoneGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'devicesOfZoneGrid',
    itemId: 'allDevicesOfZoneGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
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
    sortableColumns: true,
    forceFit: true,
    enableColumnMove: true,
    config: {
        service: null
    },
    searchLink: null,

    initComponent: function () {
        var me = this
            searchCriteria = me.service;
            /*srouter = me.getController('Uni.controller.history.Router');
            allDevicesInSearchLink = {
                xtype: 'container',
                margin: '0 0 4 7',
                html: Ext.String.format('<a href="{0}">{1}</a>',
                    router.getRoute('devices/device/zones').buildUrl(),
                    Uni.I18n.translate('deviceZones.viewAllDevicesInSearch', 'MDC', 'View all devices in search')
                )
            }*/;

        this.columns = [
            {
                header: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                dataIndex: 'name',
                sortable: true,
                hideable: false,
                renderer: function (value, b, record) {
                    return '<a href="#/devices/' + encodeURIComponent(record.get('name')) + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
                dataIndex: 'deviceTypeName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                dataIndex: 'deviceConfigurationName',
                sortable: false,
                hideable: false,
                fixed: true,
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn-remove',
                itemId: 'device-zone-remove-button',
                privileges: Cfg.privileges.Validation.adminZones,
                handler: function (grid, rowIndex, colIndex, column, event, deviceZoneRecord) {
                    me.fireEvent('zoneDeviceRemoveEvent', deviceZoneRecord);
                }
            }
        ];

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
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'view-all-devices-in-search',
                        text: Uni.I18n.translate('deviceZones.Search', 'MDC', 'View all devices in search'),
                        //privileges: Mdc.privileges.DeviceType.admin,
                        href: me.searchLink
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

        me.callParent(arguments);
    }
});



