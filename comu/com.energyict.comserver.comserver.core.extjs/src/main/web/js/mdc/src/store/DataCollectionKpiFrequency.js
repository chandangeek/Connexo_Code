Ext.define('Mdc.store.DataCollectionKpiFrequency', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'every', type: 'auto'}
    ],

    data: [
        {
            name: 5 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 5, 'MDC', 'minutes'),
            every: {
                "every": {
                    "count": 5,
                    "timeUnit": "minutes"
                }
            }
        },
        {
            name: 15 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 15, 'MDC', 'minutes'),
            every: {
                "every": {
                    "count": 15,
                    "timeUnit": "minutes"
                }
            }
        },
        {
            name: 30 + ' ' + Uni.I18n.translatePlural('general.timeUnit.minutes', 30, 'MDC', 'minutes'),
            every: {
                "every": {
                    "count": 30,
                    "timeUnit": "minutes"
                }
            }
        },
        {
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', 'hour'),
            every: {
                "every": {
                    "count": 1,
                    "timeUnit": "hours"
                },
                lastDay: false
            }
        },
        {
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day'),
            every: {
                "every": {
                    "count": 1,
                    "timeUnit": "days"
                },
                lastDay: false
            }
        },
        {
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month'),
            every: {
                "every": {
                    "count": 1,
                    "timeUnit": "months"
                },
                lastDay: false
            }
        }
    ]
});
