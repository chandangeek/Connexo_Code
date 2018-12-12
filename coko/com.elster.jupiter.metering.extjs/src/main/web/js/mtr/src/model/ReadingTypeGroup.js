/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.model.ReadingTypeGroup', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'numberOfReadingTypes', type: 'number'},

        {name: 'commodity', type: 'string'},
        {name: 'measurementKind', type: 'string'},
        {name: 'flowDirection', type: 'string'},
        {name: 'unit', type: 'string'},

        {name: 'macroPeriod', type: 'string'},
        {name: 'timePeriod', type: 'string'},
        {name: 'accumulation', type: 'string'},
        {name: 'aggregate', type: 'string'},

        {name: 'multiplier', type: 'number', useNull: true},
        {name: 'phases', type: 'string'},
        {name: 'tou', type: 'number', useNull: true},
        {name: 'cpp', type: 'number', useNull: true},
        {name: 'consumptionTier', type: 'number', useNull: true}
    ],
    proxy: {
        type: 'rest',
        url: '../../api/mtr/readingtypes/groups',
        reader: {
            type: 'json',

        }
    }
});
