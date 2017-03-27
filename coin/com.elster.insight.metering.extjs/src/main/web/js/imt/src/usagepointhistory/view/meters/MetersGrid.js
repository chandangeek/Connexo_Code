/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.meters.MetersGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Imt.usagepointhistory.store.HistoricalMeters'
    ],
    alias: 'widget.usage-point-history-meters-grid',
    router: null,
    store: 'Imt.usagepointhistory.store.HistoricalMeters',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.current', 'IMT', 'Current'),
                dataIndex: 'current',
                width: 80,
                renderer: function (value) {
                    return !!value ? '<i class="icon icon-checkmark-circle"></i>' : null;
                }
            },
            {
                header: Uni.I18n.translate('general.period', 'IMT', 'Period'),
                renderer: function (value, meta, record) {
                    var from = record.get('start'),
                        to = record.get('end');

                    return to ? Uni.I18n.translate('general.period.fromUntil', 'IMT', 'From {0} until {1}', [
                        Uni.DateTime.formatDateTimeShort(from),
                        Uni.DateTime.formatDateTimeShort(to)
                    ])
                        : Uni.I18n.translate('general.period.from', 'IMT', 'From {0}', [
                        Uni.DateTime.formatDateTimeShort(from)
                    ]);
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                dataIndex: 'meterRole',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                dataIndex: 'meter',
                renderer: function (value, meta, record) {
                    var url = record.get('url'),
                        meter = Ext.String.htmlEncode(value);

                    return url ? '<a href="' + url + '" target="_blank">' + meter + '</a>' : meter;
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.ongoingProcesses', 'IMT', 'Ongoing processes'),
                dataIndex: 'ongoingProcesses',
                renderer: function (value) {
                    return value ? value.length : '-';
                },
                flex: 1
            }
        ];
        me.callParent(arguments);
    }
});

