/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.FormInfoMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-info-message',
    text: null,
    ui: 'small',
    style: 'border: 1px solid #71adc7; border-radius: 10px;',
    margin: '7 0 32 0',
    htmlEncode: true,
    iconCmp: {
        xtype: 'component',
        style: 'font-size: 22px; color: #71adc7; margin: 0px -22px 0px 0px;',
        cls: 'icon-info'
    },
    bodyStyle: 'color: #686868; padding: 5px 0 5px 32px',
    shrinkWrap: true,

    initComponent: function() {
        var me = this;

        me.lbar = me.iconCmp;
        me.html = me.text;

        me.callParent(arguments);
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.update(text);
    }

});