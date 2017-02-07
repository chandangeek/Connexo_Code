/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskActionPreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'comtaskActionPreviewForm',
    border: false,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'displayfield',
        labelWidth: 350,
        width: 400
    },

    items: [
    ],

    addAttribute: function(labelText, value) {
        var me = this,
            field = Ext.create('Ext.form.field.Display', Ext.apply(me.defaults, {
                fieldLabel: labelText,
                value: value,
                renderer: function(value) {
                    return value.replace(/\n/g, '<br>');
                }
            }));

        me.add(field);
    },

    addNoAttributesInfo: function() {
        this.add({
            xtype: 'container',
            layout: 'column',
            items: {
                xtype: 'uni-form-empty-message',
                itemId: 'empty-message',
                text: Uni.I18n.translate('general.actionHasNoAttributes', 'MDC', 'This action has no attributes that need to be defined')
            }
        });
    },

    reinitialize: function() {
        this.removeAll();
    }
});