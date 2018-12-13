/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.model.AddReadingTypeGroup', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true, persist: false},
        {name: 'mRID', type: 'string', defaultValue: null},
        {name: 'aliasName', type: 'string', defaultValue: null},

        {
            name: 'basicMacroPeriod', type: 'auto',
            defaultValue: null,
            convert: function (v, record) {
                var value = v;
                return v ? (v & 0x0000FFFF) : v;
            }
        },
        {name: 'basicAggregate', type: 'auto', defaultValue: null},
        {name: 'basicMeasuringPeriod', type: 'auto', defaultValue: null},
        {name: 'basicAccumulation', type: 'auto', defaultValue: null},
        {name: 'basicFlowDirection', type: 'auto', defaultValue: null},
        {name: 'basicCommodity', type: 'auto', defaultValue: null},
        {name: 'basicMeasurementKind', type: 'auto', defaultValue: null},
        {name: 'basicTou', type: 'auto', defaultValue: null},
        {name: 'basicCpp', type: 'auto', defaultValue: null},
        {name: 'basicConsumptionTier', type: 'auto', defaultValue: null},
        {name: 'basicPhases', type: 'auto', defaultValue: null},
        {name: 'basicMetricMultiplier', type: 'auto', defaultValue: null},
        {name: 'basicUnit', type: 'auto', defaultValue: null},

        {name: 'macroPeriod', type: 'auto', defaultValue: null},
        {name: 'aggregate', type: 'auto', defaultValue: null},
        {name: 'measuringPeriod', type: 'auto', defaultValue: null},
        {name: 'accumulation', type: 'auto', defaultValue: null},
        {name: 'flowDirection', type: 'auto', defaultValue: null},
        {name: 'commodity', type: 'auto', defaultValue: null},
        {name: 'measurementKind', type: 'auto', defaultValue: null},
        {name: 'interHarmonicNumerator', type: 'auto', defaultValue: null},
        {name: 'interHarmonicDenominator', type: 'auto', defaultValue: null},
        {name: 'argumentNumerator', type: 'auto', defaultValue: null},
        {name: 'argumentDenominator', type: 'auto', defaultValue: null},
        {name: 'tou', type: 'auto', defaultValue: null},
        {name: 'cpp', type: 'auto', defaultValue: null},
        {name: 'consumptionTier', type: 'auto', defaultValue: null},
        {name: 'phases', type: 'auto', defaultValue: null},
        {name: 'metricMultiplier', type: 'auto', defaultValue: null},
        {name: 'unit', type: 'auto', defaultValue: null},
        {name: 'currency', type: 'auto', defaultValue: null},
        {name: 'specifyBy', type: 'auto', persist: false, defaultValue: null}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/mtr/readingtypes{path}',

        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        setUrl: function (url) {
            this.url = this.urlTpl.replace('{path}', url)
        }
    }
});

