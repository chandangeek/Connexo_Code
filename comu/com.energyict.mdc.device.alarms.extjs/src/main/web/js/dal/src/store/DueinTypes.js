Ext.define('Dal.store.DueinTypes', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.DueinType',

    data: [
        {name: 'days', displayValue: Uni.I18n.translate('period.days','ISU','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','ISU','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','ISU','month(s)')}
    ]
});
