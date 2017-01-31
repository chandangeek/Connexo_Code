/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.form.field.ComboBox', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    initComponent: function() {
        this.callParent(arguments);
    }
});


