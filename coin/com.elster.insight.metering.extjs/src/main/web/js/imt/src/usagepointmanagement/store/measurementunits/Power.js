/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.Power', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'W', unit: 'W', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.watt', 'IMT', 'W')},
        {id: 'kW', unit: 'W', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloWatt', 'IMT', 'kW')},
        {id: 'MW', unit: 'W', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaWatt', 'IMT', 'MW')},
        {id: 'GW', unit: 'W', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaWatt', 'IMT', 'GW')},
        {id: 'ТW', unit: 'W', multiplier: 12, displayValue: Uni.I18n.translate('general.measurementunits.teraWatt', 'IMT', 'ТW')},
        {id: 'VA', unit: 'VA', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.voltAmpere', 'IMT', 'VA')},
        {id: 'kVA', unit: 'VA', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloVoltAmpere', 'IMT', 'kVA')},
        {id: 'MVA', unit: 'VA', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaVoltAmpere', 'IMT', 'MVA')},
        {id: 'GVA', unit: 'VA', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaVoltAmpere', 'IMT', 'GVA')},
        {id: 'ТVA', unit: 'VA', multiplier: 12, displayValue: Uni.I18n.translate('general.measurementunits.teraVoltAmpere', 'IMT', 'ТVA')}
    ]
});