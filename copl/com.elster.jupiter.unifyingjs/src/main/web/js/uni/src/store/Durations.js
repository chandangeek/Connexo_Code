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
            localizeValue: Uni.I18n.translatePlural('general.timeUnit.years', 1, 'UNI', '{0} years', '{0} year', '{0} years')
        },
        {
            id: '6months',
            count: 6,
            timeUnit: 'months',
            localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 6, 'UNI', '{0} months', '{0} month', '{0} months')
        },
        {
            id: '3months',
            count: 3,
            timeUnit: 'months',
            localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 3, 'UNI', '{0} months', '{0} month', '{0} months')
        },
        {
            id: '1months',
            count: 1,
            timeUnit: 'months',
            localizeValue: Uni.I18n.translatePlural('general.timeUnit.months', 1, 'UNI', '{0} months', '{0} month', '{0} months')
        },
        {
            id: '1weeks',
            count: 1,
            timeUnit: 'weeks',
            localizeValue: Uni.I18n.translatePlural('general.timeUnit.weeks', 1, 'UNI', '{0} weeks', '{0} week', '{0} weeks')
        }
    ]
});