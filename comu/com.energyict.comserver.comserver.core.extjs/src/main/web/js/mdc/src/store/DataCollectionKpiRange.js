Ext.define('Mdc.store.DataCollectionKpiRange', {
    extend: 'Ext.data.Store',
    fields: [
        {name: 'id', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'value', type: 'auto'}
    ],

    data: [
        {
            id: '1hours',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', 'hour'),
            value: {
                count: 1,
                timeUnit: 'hours'
            }
        },
        {
            id: '1days',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', 'day'),
            value: {
                count: 1,
                timeUnit: 'days'
            }
        },
        {
            id: '1weeks',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', 'week'),
            value: {
                count: 1,
                timeUnit: 'weeks'
            }
        },
        {
            id: '2weeks',
            name: 2 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', 'weeks'),
            value: {
                count: 2,
                timeUnit: 'weeks'
            }
        },
        {
            id: '1months',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', 'month'),
            value: {
                count: 1,
                timeUnit: 'months'
            }
        },
        {
            id: '1years',
            name: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', 'year'),
            value: {
                count: 1,
                timeUnit: 'years'
            }
        }
    ],

    associationFrequencyRange: {
        '5minutes': ['1hours', '1days', '1weeks'],
        '15minutes': ['1hours', '1days', '1weeks', '2weeks', '1months'],
        '30minutes': ['1days', '1weeks', '2weeks'],
        '1hours': ['1days', '1weeks', '2weeks'],
        '1days': ['1weeks', '2weeks', '1months', '1years'],
        '1months': ['1years']
    },

    filterByFrequency: function (frequency) {
        var me = this,
            availableRanges;

        if (frequency) {
            availableRanges = me.associationFrequencyRange[frequency];
            me.filterBy(function (record) {
                return !!_.find(availableRanges, function (value) {
                    return value === record.getId();
                });
            });
        }
    }
});
