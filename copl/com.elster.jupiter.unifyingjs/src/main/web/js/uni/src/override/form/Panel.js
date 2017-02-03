/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.form.Panel', {
    override: 'Ext.form.Panel',
    buttonAlign: 'left',

    initComponent: function() {
        var me = this;
        var width = 100;

        if (me.defaults && me.defaults.labelWidth) {
            width = me.defaults.labelWidth;
        }
        // the case when label align is defined and not left. Than don't move the buttons.
        if (me.defaults
         && me.defaults.labelAlign
         && me.defaults.labelAlign != 'left') {
            width = 0;
        }
        if (me.buttons) {
            me.buttons.splice(0, 0, {
                xtype: 'tbspacer',
                width: width,
                cls: 'x-form-item-label-right'
            })
        }

        me.callParent(arguments);
    }
});
