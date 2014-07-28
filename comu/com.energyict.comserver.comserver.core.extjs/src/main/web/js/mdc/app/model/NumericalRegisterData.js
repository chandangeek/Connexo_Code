Ext.define('Mdc.model.NumericalRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'multiplier', type:'auto', useNull: true},
        {name: 'rawValue', type:'auto', useNull: true},
        {name: 'unitOfMeasure', type:'auto', useNull: true}
    ]
});
