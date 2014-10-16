Ext.define('Mdc.model.NumericalRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'multiplier', type:'auto', useNull: true, defaultValue: null},
        {name: 'rawValue', type:'auto', useNull: true, defaultValue: null},
        {name: 'unitOfMeasure', type:'auto', useNull: true}
    ]
});
