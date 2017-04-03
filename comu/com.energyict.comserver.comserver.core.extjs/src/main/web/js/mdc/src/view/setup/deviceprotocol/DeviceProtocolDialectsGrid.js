/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceProtocolDialectsGrid',
    overflowY: 'auto',
    itemId: 'deviceprotocoldialectsgrid',
    deviceId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ProtocolDialectsOfDevice',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu',
        'Uni.util.Common'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'ProtocolDialectsOfDevice',

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                menu: {xtype: 'protocol-dialect-action-menu'},
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.protocolDialectsActions
            }

        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} protocol dialects'),
                displayMoreMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} protocol dialects'),
                emptyMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.emptyMsg', 'MDC', 'There are no protocol dialects to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceId: me.deviceId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('protocolDialects.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Protocol dialects per page')
            }
        ];

        me.callParent();
    }
});