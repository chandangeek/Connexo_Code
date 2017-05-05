/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.model.OutputEstimationConfiguration', {
    extend: 'Uni.model.Version',
    requires: [
        'Uni.property.model.Property'
    ],
    idProperty: 'ruleId',
    fields: [
        'id',
        'ruleId',
        {name: 'name', persist: false},
        {name: 'estimator', persist: false},
        {name: 'readingType', persist: false},
        {name: 'isActive', persist: false}
    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function () {
                return 'Uni.property.model.Property';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/udr/usagepoints/{usagePointId}/purposes/{purposeId}/outputs/{outputId}/estimation'
    }
});
