/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.button.widget.Small', {
    extend: 'Ext.button.Button',
    xtype: 'smallButton',

    text: 'Small',

    constructor: function(cfg) {
        if (cfg.icon) {
            this.iconCls = 'btn-add';
        }
        this.callParent(arguments);
    }
});