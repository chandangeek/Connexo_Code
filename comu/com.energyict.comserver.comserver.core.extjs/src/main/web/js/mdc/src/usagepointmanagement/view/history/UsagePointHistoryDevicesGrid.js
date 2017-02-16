/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.UsagePointHistoryDevicesGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Mdc.usagepointmanagement.store.UsagePointHistoryDevices'
    ],
    alias: 'widget.usage-point-history-devices-grid',
    router: null,
    store: 'Mdc.usagepointmanagement.store.UsagePointHistoryDevices',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.active', 'MDC', 'Active'),
                dataIndex: 'active',
                width: 80,
                renderer: function (value) {
                    if (!!value) {
                        return '<i class="icon icon-checkmark-circle" data-qtip="' + Uni.I18n.translate('general.active', 'MDC', 'Active') + '"></i>';
                    } else {
                        return null;
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                renderer: function (value, meta, record) {
                    var from = record.get('start'),
                        to = record.get('end');

                    return to ? Uni.I18n.translate('general.period.fromUntil', 'MDC', 'From {0} until {1}', [
                        Uni.DateTime.formatDateTimeShort(from),
                        Uni.DateTime.formatDateTimeShort(to)
                    ])
                        : Uni.I18n.translate('general.period.from', 'MDC', 'From {0}', [
                        Uni.DateTime.formatDateTimeShort(from)
                    ]);
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.device', 'MDC', 'Device'),
                dataIndex: 'name',
                renderer: function (value) {
                    var url = me.router.getRoute('devices/device').buildUrl({
                        deviceId: value
                    });
                    return Mdc.privileges.Device.canView() ? '<a href="' + url + '">' + value + '</a>' : value;
                },
                flex: 1
            }
        ];
        me.callParent(arguments);
    }
});

