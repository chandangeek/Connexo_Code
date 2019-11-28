/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.view.EditPropertiesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-properties-form',
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                itemId: 'cfg-properties',
                items: []
            }
        ];
        me.callParent(arguments);
    }
});
