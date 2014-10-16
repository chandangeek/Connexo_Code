Ext.define('Mdc.model.BillingRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'multiplier', type:'auto', useNull: true},
        {name: 'unitOfMeasure', type:'auto', useNull: true},
        {name: 'interval', type:'auto', useNull: true},
        {name: 'intervalStart', mapping: 'interval.start', useNull: true, persist: false},
        {name: 'intervalEnd', mapping: 'interval.end', useNull: true, persist: false}
    ]
});