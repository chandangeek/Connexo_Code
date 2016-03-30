Ext.define('Uni.property.model.MeasurementUnit', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            convert: function (value, record) {
                return record.get('multiplier') + record.get('unit');
            }
        },
        'unit',
        'multiplier',
        'displayValue'
    ]
});