/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.MessageCategory', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.Message'
    ],
    fields: [
        { name: 'name', type: 'string' }
    ],
    associations: [
        {
            type: 'hasMany',
            model: 'Mdc.model.Message',
            name: 'deviceMessageEnablements',
            instanceName: 'deviceMessageEnablements',
            associationKey: 'deviceMessageEnablements',
            getterName: 'getMessageEnablements',
            setterName: 'setMessageEnablements'
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/devicemessageenablements',
        reader: {
            type: 'json',
            root: 'categories'
        }
    }
});