/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.model.StandardDataSelector', {
    extend: 'Ext.data.Model',


    fields: [
        'exportComplete',
        'exportUpdate',
        'exportContinuousData',
        {name: 'updateWindow', defaultValue: null},
        {name: 'updatePeriod', defaultValue: null},
        {name: 'deviceGroup', defaultValue: null},
        {name: 'usagePointGroup', defaultValue: null},
        {name: 'exportPeriod', defaultValue: null},
        {name: 'validatedDataOption', defaultValue: null},
        {name: 'readingTypes', defaultValue: null},
        {name: 'eventTypeCodes', defaultValue: null},
        {name: 'exportWindow', persist: false}
    ],

    idProperty: 'name',
    associations: [
        {
            type: 'hasMany',
            model: 'Dxp.model.ReadingType',
            associationKey: 'readingTypes',
            name: 'readingTypes'
        }
    ]
});