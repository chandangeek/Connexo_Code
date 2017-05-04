/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisterValidationConfiguration', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.RegisterValidationConfigurationForReadingType'
    ],
    associations: [
        {
            name: 'rulesForCalculatedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.RegisterValidationConfigurationForReadingType',
            associationKey: 'rulesForCalculatedReadingType',
            foreignKey: 'rulesForCalculatedReadingType'
        },
        {
            name: 'rulesForCollectedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.RegisterValidationConfigurationForReadingType',
            associationKey: 'rulesForCollectedReadingType',
            foreignKey: 'rulesForCollectedReadingType'
        }
    ]
});
