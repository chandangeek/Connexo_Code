/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.FormEmptyMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-empty-message',
    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
    ui: 'small',
    framed: true,
    text: null,
    text2Measure: null,
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    style: {
        borderColor: '#71adc7',
        paddingTop: '10px',
        paddingBottom: '10px',
        paddingLeft: '10px',
        paddingRight: '10px'
    },
    margin: '7 0 32 0',
    htmlEncode: true,
    doAutoSize: true,
    beforeRender: function () {
        var me = this;
        me.renew();
        if (me.doAutoSize) {
            me.on('afterrender', me.adaptSizeToText, me, {single: true});
        }
        me.callParent(arguments)
    },

    renew: function () {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll(true);

        me.text2Measure = me.text;
        if (me.htmlEncode) {
            me.text = Ext.String.htmlEncode(me.text);
        }

        me.add([
            {
                xtype: 'box',
                height: 22,
                width: 26,
                style: {
                    fontSize: '22px',
                    color: '#71adc7'
                },
                cls: 'icon-info'
            },
            {
                ui: 'form-error',
                itemId: 'errorMessage',
                html: me.text,
                style: {
                    marginLeft: '10px'
                }
            }
        ]);

        Ext.resumeLayouts();
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
        me.adaptSizeToText();
        me.doLayout();
    },

    adaptSizeToText: function () {
        var me = this,
            metrics = new Ext.util.TextMetrics(me.down('#errorMessage').el),
            width = metrics.getWidth(me.text2Measure);
        me.maxWidth = width + 70;
    }

});
