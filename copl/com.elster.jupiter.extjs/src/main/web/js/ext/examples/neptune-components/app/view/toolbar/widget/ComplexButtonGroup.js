/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.ComplexButtonGroup', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'complexButtonGroupToolbar',

    items: [
        { xtype: 'complexButtonGroup' },
        { xtype: 'simpleButtonGroup' }
    ]
});