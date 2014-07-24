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
            translation: Uni.I18n.translate('schedulefield.monday', 'UNI', 'monday')
        },
        {
            id: 2,
            key: 'tuesday',
            translation: Uni.I18n.translate('schedulefield.tuesday', 'UNI', 'tuesday')
        },
        {
            id: 3,
            key: 'wednesday',
            translation: Uni.I18n.translate('schedulefield.wednesday', 'UNI', 'wednesday')
        },
        {
            id: 4,
            key: 'thursday',
            translation: Uni.I18n.translate('schedulefield.thursday', 'UNI', 'thursday')
        },
        {
            id: 5,
            key: 'friday',
            translation: Uni.I18n.translate('schedulefield.friday', 'UNI', 'friday')
        },
        {
            id: 6,
            key: 'saturday',
            translation: Uni.I18n.translate('schedulefield.saturday', 'UNI', 'saturday')
        },
        {
            id: 7,
            key: 'sunday',
            translation: Uni.I18n.translate('schedulefield.sunday', 'UNI', 'sunday')
        }
    ]
});