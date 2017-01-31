/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.model.ReadingType', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'mRID', type: 'string'},
        {name: 'active', type: 'boolean'},
        {name: 'aliasName', type: 'string'},
        {name: 'name', type: 'string'},
        {name: 'macroPeriod', type: 'string'},
        {name: 'aggregate', type: 'string'},
        {name: 'measuringPeriod', type: 'string'},
        {name: 'accumulation', type: 'string'},
        {name: 'flowDirection', type: 'string'},
        {name: 'commodity', type: 'string'},
        {name: 'measurementKind', type: 'string'},
        {name: 'interHarmonicNumerator', type: 'number', useNull: true},
        {name: 'interHarmonicDenominator', type: 'number', useNull: true},
        {name: 'argumentNumerator', type: 'number', useNull: true},
        {name: 'argumentDenominator', type: 'number', useNull: true},
        {name: 'tou', type: 'number', useNull: true},
        {name: 'cpp', type: 'number', useNull: true},
        {name: 'consumptionTier', type: 'number', useNull: true},
        {name: 'phases', type: 'string'},
        {name: 'metricMultiplier', type: 'number', useNull: true},
        {name: 'unit', type: 'string'},
        {name: 'currency', type: 'string'},
        {name: 'version', type: 'number', useNull: true},
        {name: 'names', type: 'auto', useNull: true, defaultValue: {}},
        {name: 'fullAliasName', type: 'string'}
    ],
    proxy: {
        type: 'rest',
        urlTpl: '../../api/mtr/readingtypes/{mRID}',
        reader: {
            type: 'json',
            root: 'readingTypes'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});
