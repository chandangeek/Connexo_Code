/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.Capacity', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'Wh', unit: 'Wh', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.wattHours', 'IMT', 'Wh')},
        {id: 'kWh', unit: 'Wh', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloWattHours', 'IMT', 'kWh')},
        {id: 'MWh', unit: 'Wh', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaWattHours', 'IMT', 'MWh')},
        {id: 'GWh', unit: 'Wh', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaWattHours', 'IMT', 'GWh')}
    ]
});