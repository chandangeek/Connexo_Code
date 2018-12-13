/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.timeofuse.DayType
 */
Ext.define('Uni.model.timeofuse.DayType', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.model.timeofuse.Range'
    ],
    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'}
    ],

    associations: [
        {
            name: 'ranges',
            type: 'hasMany',
            model: 'Uni.model.timeofuse.Range',
            associationKey: 'ranges',
            foreignKey: 'ranges',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.timeofuse.Range';
            }
        }
    ]

});