/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.PredefinedPropertyValue', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.PossibleValue'
    ],

    fields: [
        {name: 'exhaustive', type: 'boolean'},
        {name: 'selectionMode', type: 'string'},
        {name: 'possibleValues', type: 'auto'}
    ],
    associations: [
        {name: 'possibleValues', type: 'hasMany', model: 'Uni.property.model.PossibleValue', associationKey: 'possibleValues'}
    ]
});