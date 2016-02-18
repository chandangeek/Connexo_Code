Ext.define('Imt.usagepointmanagement.store.measurementunits.Volume', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeasurementUnit',
    data: [
        {id: 'm3/h', unit: 'm3/h', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.cubicMeterPerHour', 'IMT', 'm3/h')}
    ]
});