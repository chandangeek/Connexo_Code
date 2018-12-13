/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.CustomTaskType', {
    extend: 'Ext.data.Model',
    requires: [
        'Apr.model.TaskProperties'
    ],
    fields: [
        'name',
        'displayName',
        'properties',
        'actions'

    ],
    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Apr.model.TaskProperties',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Apr.model.TaskProperties';
            }
        }
    ],

});
