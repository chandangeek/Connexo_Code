/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ChannelValidationConfiguration', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ChannelValidationConfigurationForReadingType'
    ],
    associations: [
        {
            name: 'rulesForCalculatedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.ChannelValidationConfigurationForReadingType',
            associationKey: 'rulesForCalculatedReadingType',
            foreignKey: 'rulesForCalculatedReadingType'
        },
        {
            name: 'rulesForCollectedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.ChannelValidationConfigurationForReadingType',
            associationKey: 'rulesForCollectedReadingType',
            foreignKey: 'rulesForCollectedReadingType'
        }
    ]
});
