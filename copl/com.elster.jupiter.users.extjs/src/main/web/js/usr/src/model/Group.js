/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.Group', {
    extend: 'Uni.model.Version',
    requires: [
        'Usr.model.Privilege'
    ],
    fields: [
        {name: 'id', type: 'int', useNull: true},
        'name',
        'description',
        'version',
        'createdOn',
        'modifiedOn',
        'selected',
        'canEdit',
        'currentUserCanGrant',
        {
            name: 'disableGrantCheckbox',
            persist: false,
            mapping: function (data) {
                return !data.currentUserCanGrant;
            }
        }
    ],


    idProperty: 'id',
    associations: [
        {
            type: 'hasMany',
            model: 'Usr.model.Privilege',
            associationKey: 'privileges',
            name: 'privileges'

        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/usr/groups',
        reader: {
            type: 'json'
        }
    }
});