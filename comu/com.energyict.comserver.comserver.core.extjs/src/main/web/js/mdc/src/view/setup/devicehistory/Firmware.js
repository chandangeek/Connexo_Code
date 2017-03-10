/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicehistory.Firmware', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-history-firmware-panel',
    itemId: 'mdc-device-history-firmware-grid',
    store: 'Mdc.store.DeviceFirmwareHistory',
    forceFit: true,
    autoScroll: false,
    enableColumnHide: false,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.version', 'MDC', 'Version'),
                dataIndex: 'firmwareVersion',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'firmwareType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.activationDate', 'MDC', 'Activation date'),
                dataIndex: 'activationDate',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '';
                }
            }
        ];
        me.callParent(arguments);
    }

});