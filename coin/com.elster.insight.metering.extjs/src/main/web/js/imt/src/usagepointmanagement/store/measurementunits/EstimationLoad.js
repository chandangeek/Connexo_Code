/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.EstimationLoad', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'A', unit: 'A', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.ampere', 'IMT', 'A')},
        {id: 'kA', unit: 'A', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloAmpere', 'IMT', 'kA')},
        {id: 'MA', unit: 'A', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaAmpere', 'IMT', 'MA')},
        {id: 'VA', unit: 'VA', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.voltAmpere', 'IMT', 'VA')},
        {id: 'kVA', unit: 'VA', multiplier: 3, displayValue: Uni.I18n.translate('general.measurementunits.kiloVoltAmpere', 'IMT', 'kVA')},
        {id: 'MVA', unit: 'VA', multiplier: 6, displayValue: Uni.I18n.translate('general.measurementunits.megaVoltAmpere', 'IMT', 'MVA')},
        {id: 'GVA', unit: 'VA', multiplier: 9, displayValue: Uni.I18n.translate('general.measurementunits.gigaVoltAmpere', 'IMT', 'GVA')},
        {id: 'ТVA', unit: 'VA', multiplier: 12, displayValue: Uni.I18n.translate('general.measurementunits.teraVoltAmpere', 'IMT', 'ТVA')}
    ]
});