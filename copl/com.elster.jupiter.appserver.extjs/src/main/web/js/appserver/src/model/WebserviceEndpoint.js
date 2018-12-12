/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.WebserviceEndpoint', {
    extend: 'Uni.model.Version',
    requires: [
        'Apr.model.LogLevel',
        'Apr.model.AuthenticationMethod',
        'Apr.model.Role'
    ],
    fields: [
        {name: 'id', type: 'number'},
        {name: 'version', type: 'number'},
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
            model: 'Apr.model.LogLevel',
            associationKey: 'logLevel',
            name: 'logLevel',
            getterName: 'getLogLevel',
            setterName: 'setLogLevel'
        },
        {
            model: 'Apr.model.AuthenticationMethod',
            associationKey: 'authenticationMethod',
            name: 'authenticationMethod',
            getterName: 'getAuthenticationMethod',
            setterName: 'setAuthenticationMethod'
        },
        {
            model: 'Apr.model.Role',
            associationKey: 'group',
            name: 'group',
            getterName: 'getGroup',
            setterName: 'setGroup'
        }
    ]
});