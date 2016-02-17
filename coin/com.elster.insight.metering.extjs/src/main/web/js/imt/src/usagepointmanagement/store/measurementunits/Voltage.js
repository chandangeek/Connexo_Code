Ext.define('Imt.usagepointmanagement.store.measurementunits.Voltage', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeasurementUnit',
    data: [
        {id: 'V', unit: 'V', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.volt', 'IMT', 'V')},
        {id: 'kV', unit: 'V', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloVolt', 'IMT', 'kV')},
        {id: 'MV', unit: 'V', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaVolt', 'IMT', 'MV')},
        {id: 'GV', unit: 'V', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaVolt', 'IMT', 'GV')}
    ]
});