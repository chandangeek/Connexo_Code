/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceTypesGrid',
    overflowY: 'auto',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceTypes',
        'Mdc.store.DeviceTypePurposes',
        'Mdc.view.setup.devicetype.DeviceTypeActionMenu',
        'Ext.ux.exporter.ExporterButton'
    ],
    store: 'Mdc.store.DeviceTypes',
    purposeStore: null,
    initComponent: function () {
        var me = this;

        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/devicetypes/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'deviceTypePurpose',
                renderer: function (value) {
                    return me.purposeStore ? me.purposeStore.findRecord('deviceTypePurpose', value).get('localizedValue') : value;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                dataIndex: 'deviceProtocolPluggableClass',
                renderer: function (value) {
                    return value ? value : '-';
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {
                    xtype: 'device-type-action-menu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} device types'),
                displayMoreMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} device types'),
                emptyMsg: Uni.I18n.translate('devicetype.pagingtoolbartop.emptyMsg', 'MDC', 'There are no device types to display'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetype.createDeviceType', 'MDC', 'Add device type'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        itemId: 'createDeviceType',
                        xtype: 'button',
                        action: 'createDeviceType'
                    }
                    ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('devicetype.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Device types per page'),
            }
        ];

        me.callParent();
    }
});
