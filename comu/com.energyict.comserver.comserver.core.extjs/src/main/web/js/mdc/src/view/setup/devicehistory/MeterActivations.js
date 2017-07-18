/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicehistory.MeterActivations', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-history-meter-activations-tab',
    requires: [
        'Mdc.store.device.MeterActivations'
    ],
    store: 'Mdc.store.device.MeterActivations',
    initComponent: function () {
        var me = this,
            router = me.router;

        me.columns = [
            {
                header: Uni.I18n.translate('general.active', 'MDC', 'Active'),
                dataIndex: 'active',
                width: 80,
                renderer: function(value) {
                    if (!!value) {
                        return '<i class="icon icon-checkmark-circle" data-qtip="' + Uni.I18n.translate('general.active', 'MDC', 'Active') + '"></i>';
                    } else {
                        return null;
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                renderer: function(value, meta, record) {
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
                header: Uni.I18n.translate('general.deviceConfiguration', 'MDC', 'Device configuration'),
                dataIndex: 'deviceConfiguration',
                renderer: function(value) {
                    if (value) {
                        if (Mdc.privileges.DeviceType.canView()) {
                            var url = router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                                deviceTypeId: me.device.get('deviceTypeId'),
                                deviceConfigurationId: value.id
                            });
                            return '<a href="' + url + '">' + Ext.htmlEncode(value.name) + '</a>';
                        } else {
                            return value.name;
                        }
                    } else {
                        return '-';
                    }
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                dataIndex: 'multiplier',
                width: 100
            },
            {
                header: Uni.I18n.translate('general.usagePoint', 'MDC', 'Usage point'),
                dataIndex: 'usagePoint',
                flex: 1,
                renderer: function(value) {
                    var appName = 'Insight',
                        url;
                    if (value) {
                        if (Uni.store.Apps.checkApp(appName)) {
                            if (Mdc.privileges.UsagePoint.canViewInInsight()) {
                                url = Ext.String.format('{0}/usagepoints/{1}', Uni.store.Apps.getAppUrl(appName), encodeURIComponent(value.name));
                                return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value.name));
                            }
                        } else if (Mdc.privileges.UsagePoint.canView()) {
                            url = me.router.getRoute('usagepoints/usagepoint').buildUrl({usagePointId: value.name});
                            return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value.name));
                        }
                        return value.name;
                    } else {
                        return '-';
                    }
                }
            }
        ];
        me.callParent(arguments);
    }
});