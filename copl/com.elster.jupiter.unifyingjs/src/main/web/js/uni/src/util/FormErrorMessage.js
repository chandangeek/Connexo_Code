Ext.define('Uni.util.FormErrorMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-error-message',
    ui: 'form-error-framed',
    text: null,
    defaultText: 'There are errors on this page that require your attention.',
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    errorIcon: null,
    defaultErrorIcon: 'x-uni-form-error-msg-icon',
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
                xtype: 'box',
                height: 22,
                width: 26,
                style: 'font-size: 22px',
                cls: me.errorIcon
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
