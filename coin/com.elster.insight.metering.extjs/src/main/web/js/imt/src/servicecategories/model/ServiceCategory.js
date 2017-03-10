/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecategories.model.ServiceCategory', {
    extend: 'Ext.data.Model',
    fields: ['name', 'displayName', 'meterRoles'],
    idProperty: 'name'
});