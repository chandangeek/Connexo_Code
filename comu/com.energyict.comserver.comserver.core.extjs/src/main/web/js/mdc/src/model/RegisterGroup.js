/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.RegisterGroup', {
    extend: 'Uni.model.Version',
    requires: [
        'Mdc.model.RegisterType'
    ],
    fields: [
        {name: 'id', type: 'number', useNull: true},
        {name: 'name', type: 'string', useNull: true},
        {name: 'registerTypes'}
    ],
    associations: [
        {name: 'registerTypes', type: 'hasMany', model: 'Mdc.model.RegisterType', associationKey: 'registerTypes',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.RegisterType';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/dtc/registergroups'
    }
});