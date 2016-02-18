Ext.define('Imt.usagepointmanagement.store.measurementunits.Power', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.MeasurementUnit',
    data: [
        {id: 'W', unit: 'W', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.watt', 'IMT', 'V')},
        {id: 'kW', unit: 'W', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloWatt', 'IMT', 'kV')},
        {id: 'MW', unit: 'W', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaWatt', 'IMT', 'MV')},
        {id: 'GW', unit: 'W', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaWatt', 'IMT', 'GV')},
        {id: 'ТW', unit: 'W', multiplier: 12, displayValue: Uni.I18n.translate('general.measurementunits.teraWatt', 'IMT', 'ТV')},
        {id: 'VA', unit: 'VA', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.voltAmpere', 'IMT', 'VA')},
        {id: 'kVA', unit: 'VA', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloVoltAmpere', 'IMT', 'kVA')},
        {id: 'MVA', unit: 'VA', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaVoltAmpere', 'IMT', 'MVA')},
        {id: 'GVA', unit: 'VA', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaVoltAmpere', 'IMT', 'GVA')},
        {id: 'ТVA', unit: 'VA', multiplier: 12, displayValue: Uni.I18n.translate('general.measurementunits.teraVoltAmpere', 'IMT', 'ТVA')}
    ]
});