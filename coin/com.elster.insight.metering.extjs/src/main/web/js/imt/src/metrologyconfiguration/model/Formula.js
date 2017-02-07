/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.Formula', {
    extend: 'Ext.data.Model',
    requires: [
        'Imt.metrologyconfiguration.model.ReadingTypeRequirement',
        'Imt.metrologyconfiguration.model.CustomProperty'
    ],
    fields: ['description'],

    associations: [
        {
            name: 'readingTypeRequirements',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.ReadingTypeRequirement',
            associationKey: 'readingTypeRequirements'
        },
        {
            name: 'customProperties',
            type: 'hasMany',
            model: 'Imt.metrologyconfiguration.model.CustomProperty',
            associationKey: 'customProperties'
        }
    ]
});