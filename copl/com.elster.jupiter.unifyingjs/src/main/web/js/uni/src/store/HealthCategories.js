Ext.define('Uni.store.HealthCategories', {
    extend: 'Ext.data.Store',
    storeId: 'healthCategories',
    fields: [
        'type','displayValue'
    ],
    data: [
        {type: 'all',displayValue: Uni.I18n.translate('HealthCategories.all', 'UNI', 'All')},
        {type: 'issue',displayValue: Uni.I18n.translate('HealthCategories.issue', 'UNI', 'Issue')},
        {type: 'servicecall',displayValue: Uni.I18n.translate('HealthCategories.serviceCall', 'UNI', 'Service call')},
        {type: 'alarm',displayValue: Uni.I18n.translate('HealthCategories.alarm', 'UNI', 'Alarm')},
        {type: 'process',displayValue: Uni.I18n.translate('HealthCategories.process', 'UNI', 'Process')}
    ]
});
