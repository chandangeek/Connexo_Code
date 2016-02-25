Ext.define('Imt.usagepointmanagement.store.measurementunits.PressureExtended', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'Pa', unit: 'Pa', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.pascal', 'IMT', 'Pa')},
        {id: 'kPa', unit: 'Pa', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloPascal', 'IMT', 'kPa')},
        {id: 'MPa', unit: 'Pa', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaPascal', 'IMT', 'MPa')},
        {id: 'psi', unit: 'ps/A', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.poundsPeSquareInch', 'IMT', 'psi')},
        {id: 'kpsi', unit: 'ps/A', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloPoundsPeSquareInch', 'IMT', 'kpsi')},
        {id: 'Mpsi', unit: 'ps/A', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaPoundsPeSquareInch', 'IMT', 'Mpsi')},
        {id: 'bar', unit: 'bar', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.bar', 'IMT', 'bar')},
        {id: 'kbar', unit: 'bar', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kilobar', 'IMT', 'kbar')},
        {id: 'Mbar', unit: 'bar', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megabar', 'IMT', 'Mbar')}
    ]
});