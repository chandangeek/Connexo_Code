Ext.define('Mdc.store.DataCollectionKpiFrequency', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'value', type: 'auto'}
    ],

    data: [
        {
            id: '5minutes',
            name: 5 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 5, 'MDC', 'minutes'),
            value: {
                every: {
                    count: 5,
                    timeUnit: 'minutes'
                }
            }
        },
        {
            id: '15minutes',
            name: 15 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 15, 'MDC', 'minutes'),
            value: {
                every: {
                    count: 15,
                    timeUnit: 'minutes'
                }
            }
        },
        {
            id: '30minutes',
            name: 30 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 30, 'MDC', 'minutes'),
            value: {
                every: {
                    count: 30,
                    timeUnit: 'minutes'
                }
            }
        },
        {
            id: '1hours',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', 'hour'),
            value: {
                every: {
                    count: 1,
                    timeUnit: 'hours'
                },
                lastDay: false
            }
        },
        {
            id: '1days',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day'),
            value: {
                every: {
                    count: 1,
                    timeUnit: 'days'
                },
                lastDay: false
            }
        },
        {
            id: '1months',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month'),
            value: {
                every: {
                    count: 1,
                    timeUnit: 'months'
                },
                lastDay: false
            }
        }
    ]
});
