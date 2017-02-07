Ext.define('Mdc.model.NumericalRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'rawValue', type:'auto', useNull: true, defaultValue: null},
        {name: 'calculatedValue', type:'string'},
        {name: 'calculatedUnit', type:'string'},
        {name: 'multiplier', type:'auto'},
        {name: 'interval', type:'auto', useNull: true},
        {name: 'interval.start', mapping: 'interval.start', useNull: true, persist: false},
        {name: 'interval.end', mapping: 'interval.end', useNull: true, persist: false},
        {name: 'eventDate', type:'number', useNull: true}
    ]
});
