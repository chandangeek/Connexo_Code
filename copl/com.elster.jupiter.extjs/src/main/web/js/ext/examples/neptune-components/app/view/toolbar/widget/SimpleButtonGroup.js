/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.SimpleButtonGroup', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'simpleButtonGroupToolbar',

    items: [
        { xtype: 'simpleButtonGroup' },
        { xtype: 'smallSplitButton' }
    ]
});