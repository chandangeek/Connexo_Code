Ext.define('Mdc.model.UnitOfMeasure', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id',type:'number',useNull:true},
        {name: 'localizedValue', type: 'string', useNull: true}
    ]
});