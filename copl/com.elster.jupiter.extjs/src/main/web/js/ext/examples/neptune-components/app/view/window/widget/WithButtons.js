/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.window.widget.WithButtons', {
    extend: 'Neptune.view.window.widget.Basic',
    xtype: 'windowWithButtons',
    buttons: [
        { text: 'Submit' },
        { text: 'Cancel' }
    ]
});