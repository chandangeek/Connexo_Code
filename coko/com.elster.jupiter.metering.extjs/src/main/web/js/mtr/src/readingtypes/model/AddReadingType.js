/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.model.AddReadingType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'int', useNull: true, persist: false},
        {name: 'mRID', type: 'string', defaultValue: null},
        {name: 'aliasName', type: 'string', defaultValue: null},
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

