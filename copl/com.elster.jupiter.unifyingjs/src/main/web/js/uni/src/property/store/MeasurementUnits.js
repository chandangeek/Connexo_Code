Ext.define('Uni.property.store.MeasurementUnits', {
    extend: 'Ext.data.Store',
    model: 'Uni.property.model.MeasurementUnit',

    findUnit: function (data) {
        var me = this,
            index = me.findBy(function (record) {
                return data.unit === record.get('unit') && data.multiplier === record.get('multiplier');
            });

        return me.getAt(index);
    }
});