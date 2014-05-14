Ext.define('Mdc.model.ItemSort', {
    extend: 'Uni.component.sort.model.Sort',

    fields: [
        {
            name: 'mRID',
            displayValue: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID')
        },
        {
            name: 'serialNumber',
            displayValue: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number')
        }
    ]
});
