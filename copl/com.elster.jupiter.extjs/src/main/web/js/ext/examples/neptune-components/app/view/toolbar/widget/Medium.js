/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.Medium', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'mediumToolbar',

    items: [
        { xtype: 'mediumButton' },
        { xtype: 'mediumMenuButton' },
        { xtype: 'mediumSplitButton', icon: true }
    ]
});