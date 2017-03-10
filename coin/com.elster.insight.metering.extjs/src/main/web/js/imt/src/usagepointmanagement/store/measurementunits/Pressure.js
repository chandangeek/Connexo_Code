/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.Pressure', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'Pa', unit: 'Pa', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.pascal', 'IMT', 'Pa')},
        {id: 'kPa', unit: 'Pa', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloPascal', 'IMT', 'kPa')},
        {id: 'MPa', unit: 'Pa', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaPascal', 'IMT', 'MPa')}
    ]
});