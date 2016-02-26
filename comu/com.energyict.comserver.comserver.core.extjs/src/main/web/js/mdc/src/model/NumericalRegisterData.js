Ext.define('Mdc.model.NumericalRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'rawValue', type:'auto', useNull: true, defaultValue: null},
        {name: 'calculatedValue', type:'string'},
        {name: 'calculatedUnit', type:'string'},
        {name: 'multiplier', type:'auto'}
    ]
});
