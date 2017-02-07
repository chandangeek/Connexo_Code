Ext.define('Mdc.model.BillingRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'interval', type:'auto', useNull: true},
        {name: 'interval.start', mapping: 'interval.start', useNull: true, persist: false},
        {name: 'interval.end', mapping: 'interval.end', useNull: true, persist: false},
        {name: 'calculatedValue', type: 'string'},
        {name: 'calculatedUnit', type: 'string'},
        {name: 'eventDate', type:'number', useNull: true}
    ]
});