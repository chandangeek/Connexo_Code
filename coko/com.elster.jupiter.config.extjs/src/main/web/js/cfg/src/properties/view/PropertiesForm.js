/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.properties.view.PropertiesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.cfg-config-properties-form',

    myTooltip: Ext.create('Ext.tip.ToolTip', {
        renderTo: Ext.getBody()
    }),


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
        me.callParent();
    }
});