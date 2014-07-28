Ext.define('Mdc.model.EventRegisterData', {
    extend: 'Mdc.model.RegisterData',
    fields: [
        {name: 'multiplier', type:'auto', useNull: true},
        {name: 'unitOfMeasure', type:'auto', useNull: true},
        {name: 'interval', type:'auto', useNull: true}
    ]
});