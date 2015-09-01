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
            name: Uni.I18n.translatePlural('general.timeUnit.hours', 1, 'MDC', '{0} hours', '{0} hour', '{0} hours'),
            value: {
                count: 1,
                timeUnit: 'hours'
            }
        },
        {
            id: '1days',
            name: Uni.I18n.translatePlural('general.timeUnit.days', 1, 'MDC', '{0} days', '{0} day', '{0} days'),
            value: {
                count: 1,
                timeUnit: 'days'
            }
        },
        {
            id: '1weeks',
            name: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'MDC', '{0} weeks', '{0} week', '{0} weeks'),
            value: {
                count: 1,
                timeUnit: 'weeks'
            }
        },
        {
            id: '2weeks',
            name: Uni.I18n.translatePlural('general.timeUnit.weeks', 2, 'MDC', '{0} weeks', '{0} week', '{0} weeks'),
            value: {
                count: 2,
                timeUnit: 'weeks'
            }
        },
        {
            id: '1months',
            name: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'MDC', '{0} months', '{0} month', '{0} months'),
            value: {
                count: 1,
                timeUnit: 'months'
            }
        },
        {
            id: '1years',
            name: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'MDC', '{0} years', '{0} year', '{0} years'),
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
