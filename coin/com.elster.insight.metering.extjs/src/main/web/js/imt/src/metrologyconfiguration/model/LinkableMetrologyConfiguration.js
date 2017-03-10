/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.model.LinkableMetrologyConfiguration', {
    extend: 'Uni.model.Version',
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string'},
        {name: 'purposes', defaultValue: null}
    ],
    associations: [
        {name: 'customPropertySets', type: 'hasMany', model: 'Imt.customattributesonvaluesobjects.model.AttributeSetOnObject', associationKey: 'customPropertySets', foreignKey: 'customPropertySets'}
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/metrologyconfiguration',
        timeout: 240000,
        reader: {
            type: 'json',
            root: 'metrologyConfigurations'
        }
    }
});
