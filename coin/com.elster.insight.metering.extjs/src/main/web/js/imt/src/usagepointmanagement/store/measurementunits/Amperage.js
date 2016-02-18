Ext.define('Imt.usagepointmanagement.store.measurementunits.Amperage', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeasurementUnit',
    data: [
        {id: 'A', unit: 'A', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.ampere', 'IMT', 'A')},
        {id: 'kA', unit: 'A', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloAmpere', 'IMT', 'kA')},
        {id: 'MA', unit: 'A', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaAmpere', 'IMT', 'MA')}
    ]
});