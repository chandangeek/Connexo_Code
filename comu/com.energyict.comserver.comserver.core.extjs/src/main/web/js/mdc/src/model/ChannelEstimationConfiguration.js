/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.ChannelEstimationConfiguration', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.ChannelEstimationConfigurationForReadingType'
    ],
    associations: [
        {
            name: 'rulesForCalculatedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.ChannelEstimationConfigurationForReadingType',
            associationKey: 'rulesForCalculatedReadingType',
            foreignKey: 'rulesForCalculatedReadingType'
        },
        {
            name: 'rulesForCollectedReadingType',
            type: 'hasMany',
            model: 'Mdc.model.ChannelEstimationConfigurationForReadingType',
            associationKey: 'rulesForCollectedReadingType',
            foreignKey: 'rulesForCollectedReadingType'
        }
    ]
});
