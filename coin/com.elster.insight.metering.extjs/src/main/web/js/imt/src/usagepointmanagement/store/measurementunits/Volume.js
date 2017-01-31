/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.store.measurementunits.Volume', {
    extend: 'Imt.usagepointmanagement.store.measurementunits.Base',
    data: [
        {id: 'm3/h', unit: 'm3/h', multiplier: 0, displayValue: Uni.I18n.translate('general.measurementunits.cubicMeterPerHour', 'IMT', 'm³/h')}
    ]
});