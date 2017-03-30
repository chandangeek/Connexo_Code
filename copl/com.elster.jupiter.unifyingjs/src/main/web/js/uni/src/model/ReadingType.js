/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.ReadingType', {
    extend: 'Uni.model.Version',
    fields: [
        'accumulation',
        'active',
        'aggregate',
        'aliasName',
        'argumentDenominator',
        'argumentNumerator',
        'commodity',
        'consumptionTier',
        'cpp',
        'currency',
        'flowDirection',
        'fullAliasName',
        'interHarmonicDenominator',
        'interHarmonicNumerator',
        'isCumulative',
        'mRID',
        'macroPeriod',
        'measurementKind',
        'measuringPeriod',
        'metricMultiplier',
        'name',
        'names',
        'phases',
        'tou',
        'unit'
    ],

    idProperty: 'mRID'
});