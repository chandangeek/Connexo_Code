/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.model.SystemComponent', {
    extend: 'Ext.data.Model',
    fields: ['bundleId', 'application', 'bundleType', 'name', 'version', 'status']
});