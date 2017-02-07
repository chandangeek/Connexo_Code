/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.util.FormErrorMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-error-message',
    ui: 'form-error-framed',
    text: null,
    defaultText: Uni.I18n.translate('general.formErrors', 'UNI', 'There are errors on this page that require your attention.'),
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    errorIcon: null, // Icomoon icon name (string)
    defaultErrorIcon: 'icon-warning',
    margin: '7 0 32 0',
    htmlEncode: true,
    beforeRender: function () {
        var me = this;
        if (!me.text) {
            me.text = me.defaultText;
        }
        if (!me.errorIcon) {
            me.errorIcon = me.defaultErrorIcon
        }
        me.renew();
        me.callParent(arguments)
    },

    renew: function () {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll(true);

        if (me.htmlEncode) {
            me.text = Ext.String.htmlEncode(me.text);
        }

        me.add([
            {
                xtype: 'displayfield',
                renderer: function () {
                    return '<span class="' + me.errorIcon + '" style="display:inline-block; color:#eb5642; font-size:21px;"></span>';
                }
            },
            {
                ui: 'form-error',
                name: 'errormsgpanel',
                html: me.text
            }
        ]);


        Ext.resumeLayouts();
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});
