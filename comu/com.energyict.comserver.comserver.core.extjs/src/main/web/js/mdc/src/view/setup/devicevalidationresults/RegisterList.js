/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicevalidationresults.RegisterList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.mdc-register-list',
    store: 'ext-empty-store',
    router: null,

    columns: {
        items: [
            { 
				header: Uni.I18n.translate('device.dataValidation.register', 'MDC', 'Register'),
				dataIndex: 'name', 
				flex: 1,
				sortable: false, 
				fixed: true
            },
            {
                header: Uni.I18n.translate('validationResults.period', 'MDC', 'Period'),
                flex: 1,
                sortable: false,
                fixed: true,
                renderer: function (value, meta, record) {
                    var interval = record.get('intervalRecord').get('all');

                    if (record.get('interval') == null) {
                        return Uni.I18n.translate('validationResults.last', 'MDC', 'Last {0}', [Uni.util.Common.translateTimeUnit(interval.count, interval.timeUnit)]);
                    } else if (record.get('intervalEnd') && record.get('intervalInMs')) {
                        return Uni.I18n.translate('validationResults.starting', 'MDC', '{0} starting {1}', [Uni.util.Common.translateTimeUnit(interval.count, interval.timeUnit), Uni.DateTime.formatDateTimeLong(new Date(record.get('intervalStart')))], false);
                    }
                }
            },
            { 
				header: Uni.I18n.translate('validationResults.result', 'MDC', 'Result'), 
				dataIndex: 'total', 
				sortable: false, 
				fixed: true,

                renderer: function (value, meta, record) {
                    var me = this,
                        href;

                    href = me.router.getRoute('devices/device/registers/registerdata').buildUrl(
                        Ext.merge(me.router.arguments, {registerId: record.getId()}),
                        {
                            suspect: 'suspect',
                            interval: Ext.String.format('{0}-{1}{2}',
                                record.get('intervalStart'), 1, record.get('intervalRecord').get('all').timeUnit)
                        });
                    return '<a href="' + href + '">' + value + '</a>'
                }
            }
        ]
    }


});
