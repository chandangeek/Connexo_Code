Ext.define('Mdc.store.DaysOfWeek', {
    extend: 'Ext.data.Store',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        {
            name: 'key',
            type: 'string'
        },
        {
            name: 'translation',
            type: 'string'
        }
    ],
    data: [
        {
            id: 1,
            key: 'monday',
            translation: Uni.I18n.translate('schedulefield.monday', 'MDC', 'Monday')
        },
        {
            id: 2,
            key: 'tuesday',
            translation: Uni.I18n.translate('schedulefield.tuesday', 'MDC', 'Tuesday')
        },
        {
            id: 3,
            key: 'wednesday',
            translation: Uni.I18n.translate('schedulefield.wednesday', 'MDC', 'Wednesday')
        },
        {
            id: 4,
            key: 'thursday',
            translation: Uni.I18n.translate('schedulefield.thursday', 'MDC', 'Thursday')
        },
        {
            id: 5,
            key: 'friday',
            translation: Uni.I18n.translate('schedulefield.friday', 'MDC', 'Friday')
        },
        {
            id: 6,
            key: 'saturday',
            translation: Uni.I18n.translate('schedulefield.saturday', 'MDC', 'Saturday')
        },
        {
            id: 7,
            key: 'sunday',
            translation: Uni.I18n.translate('schedulefield.sunday', 'MDC', 'Sunday')
        }
    ]
});