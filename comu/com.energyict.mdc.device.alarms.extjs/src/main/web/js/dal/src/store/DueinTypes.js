Ext.define('Dal.store.DueinTypes', {
    extend: 'Ext.data.Store',
    model: 'Dal.model.DueinType',

    data: [
        {name: 'days', displayValue: Uni.I18n.translate('period.days','DAL','day(s)')},
        {name: 'weeks', displayValue: Uni.I18n.translate('period.weeks','DAL','week(s)')},
        {name: 'months', displayValue: Uni.I18n.translate('period.months','DAL','month(s)')}
    ]
});
