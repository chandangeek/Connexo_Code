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
            name: Uni.I18n.translatePlural('general.timeUnit.minutes', 5, 'MDC', '{0} minutes', '{0} minute', '{0} minutes'),
            value: {
                every: {
                    count: 5,
                    timeUnit: 'minutes'
                }
            }
        },
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
            id: '30minutes',
            name: Uni.I18n.translatePlural('general.timeUnit.minutes', 30, 'MDC', '{0} minutes', '{0} minute', '{0} minutes'),
            value: {
                every: {
                    count: 30,
                    timeUnit: 'minutes'
                }
            }
        },
        {
            id: '1hours',
            name: Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', '{0} hours', '{0} hour', '{0} hours'),
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
            name: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days'),
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
            name: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months'),
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
