/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.Basic', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'basicToolbar',

    items: [
        { xtype: 'smallButton' },
        { xtype: 'smallMenuButton', icon: true },
        { xtype: 'smallSplitButton' }
    ]
});