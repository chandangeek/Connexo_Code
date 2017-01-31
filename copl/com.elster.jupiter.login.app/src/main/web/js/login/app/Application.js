/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Login.Application', {
    name: 'Login',
    extend: 'Ext.app.Application',

    controllers: [
        'Login.controller.Base64',
        'Login.controller.Login'
    ]
});