/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DualControlInfo', {
    extend: 'Ext.data.Model',

    requires: [
        'Uni.model.PendingChange'
    ],

    fields: [
        {name: 'hasCurrentUserAccepted', type: 'boolean', defaultValue: true, persist: false},
        {name: 'pendingChangesType', type: 'auto'}
    ],

    associations: [
        {
            name: 'changes',
            type: 'hasMany',
            model: 'Uni.model.PendingChange',
            associationKey: 'changes',
            foreignKey: 'changes',
            getTypeDiscriminator: function (node) {
                return 'Uni.model.PendingChange';
            }
        }
    ]

});