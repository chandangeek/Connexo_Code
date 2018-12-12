/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.Mixed', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'mixedToolbar',

    items: [
        { xtype: 'largeButton' },
        { xtype: 'mediumMenuButton', icon: true },
        { xtype: 'smallSplitButton', arrowAlign: 'bottom', icon: true, iconAlign: 'top' }
    ]
});