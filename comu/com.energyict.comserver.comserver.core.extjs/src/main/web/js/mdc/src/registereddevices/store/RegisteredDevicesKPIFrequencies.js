/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.store.RegisteredDevicesKPIFrequencies', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'value', type: 'auto'}
    ],

    data: [
        {
            id: '15minutes',
            name: Uni.I18n.translatePlural('general.timeUnit.minutes', 15, 'MDC', '{0} minutes', '{0} minute', '{0} minutes'),
            value: {
                every: {
                    count: 15,
                    timeUnit: 'minutes'
                }
            }
        },
        {
            id: '4hours',
            name: Uni.I18n.translatePlural('general.timeUnit.hours', 4, 'MDC', '{0} hours', '{0} hour', '{0} hours'),
            value: {
                every: {
                    count: 4,
                    timeUnit: 'hours'
                },
                lastDay: false
            }
        },
        {
            id: '12hours',
            name: Uni.I18n.translatePlural('general.timeUnit.hours', 12, 'MDC', '{0} hours', '{0} hour', '{0} hours'),
            value: {
                every: {
                    count: 12,
                    timeUnit: 'hours'
                },
                lastDay: false
            }
        },
        {
            id: '1days',
            name: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days'),
            value: {
                every: {
                    count: 1,
                    timeUnit: 'days'
                },
                lastDay: false
            }
        }
    ]
});
