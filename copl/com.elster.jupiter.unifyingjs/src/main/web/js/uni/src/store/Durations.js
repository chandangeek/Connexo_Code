Ext.define('Uni.store.Durations', {
    extend: 'Ext.data.Store',
    storeId: 'Durations',
    fields: [
        'id',
        'count',
        'timeUnit',
        'localizeValue'
    ],
    proxy: {
        type: 'memory'
    },
    data:  [
        {
            id: '1years',
            count: 1,
            timeUnit: 'years',
            localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.years', 1, 'UNI', 'year')
        },
        {
            id: '6months',
            count: 6,
            timeUnit: 'months',
            localizeValue: 6 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 6, 'UNI', 'month')
        },
        {
            id: '3months',
            count: 3,
            timeUnit: 'months',
            localizeValue: 3 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 3, 'UNI', 'month')
        },
        {
            id: '1months',
            count: 1,
            timeUnit: 'months',
            localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.months', 1, 'UNI', 'month')
        },
        {
            id: '1weeks',
            count: 1,
            timeUnit: 'weeks',
            localizeValue: 1 + ' ' + Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'UNI', 'week')
        }
    ]
});