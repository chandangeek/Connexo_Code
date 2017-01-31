/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.model.Endpoint', {
    extend: 'Uni.model.Version',
    requires: [
        'Wss.model.LogLevel',
        'Wss.model.AuthenticationMethod',
        'Wss.model.Role'
    ],
    fields: [
       // {name: 'id', type: 'number'},
       // {name: 'version', type: 'number'},
        {name: 'name', type: 'string'},
        {name: 'webServiceName', type: 'string'},
        {name: 'url', type: 'string'},
        {name: 'previewUrl', type: 'string'},
        {name: 'logLevel', type: 'auto'},
        {name: 'tracing', type: 'boolean'},
        {name: 'traceFile', type: 'string'},
        {name: 'httpCompression', type: 'boolean'},
        {name: 'schemaValidation', type: 'boolean'},
        {name: 'active', type: 'boolean'},
        {name: 'available', type: 'boolean'},
        {name: 'authenticationMethod'},
        {name: 'username', type: 'string'},
        {name: 'password', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'direction', type: 'auto'},
        {name: 'group', type: 'auto'}
    ],

    hasOne: [
        {
            model: 'Wss.model.LogLevel',
            associationKey: 'logLevel',
            name: 'logLevel',
            getterName: 'getLogLevel',
            setterName: 'setLogLevel'
        },
        {
            model: 'Wss.model.AuthenticationMethod',
            associationKey: 'authenticationMethod',
            name: 'authenticationMethod',
            getterName: 'getAuthenticationMethod',
            setterName: 'setAuthenticationMethod'
        },
        {
            model: 'Wss.model.Role',
            associationKey: 'group',
            name: 'group',
            getterName: 'getGroup',
            setterName: 'setGroup'
        }
    ],



    proxy: {
        type: 'rest',
        url: '/api/ws/endpointconfigurations',
        reader: {
            type: 'json'
        }
    }
});