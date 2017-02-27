/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.model.HistoricalMeter', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'meter',
        'url',
        'meterRole',
        'current',
        'ongoingProcesses',
        {name: 'start', dateFormat: 'time', type: 'date'},
        {name: 'end', dateFormat: 'time', type: 'date'},
        {
            name: 'period',
            mapping: function (data) {
                var from = data.start,
                    to = data.end;

                return to ? Uni.I18n.translate('general.period.fromUntil', 'IMT', 'From {0} until {1}', [
                    Uni.DateTime.formatDateTimeShort(from),
                    Uni.DateTime.formatDateTimeShort(to)
                ])
                    : Uni.I18n.translate('general.period.from', 'IMT', 'From {0}', [
                    Uni.DateTime.formatDateTimeShort(from)
                ]);
            }
        }
    ]
});