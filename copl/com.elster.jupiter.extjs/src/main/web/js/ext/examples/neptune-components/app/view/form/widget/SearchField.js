/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.form.widget.SearchField', {
    extend: 'Ext.form.field.Trigger',
    xtype: 'searchField',
    fieldLabel: 'Search Field',
    trigger1Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
    trigger2Cls: Ext.baseCSSPrefix + 'form-search-trigger'
});
