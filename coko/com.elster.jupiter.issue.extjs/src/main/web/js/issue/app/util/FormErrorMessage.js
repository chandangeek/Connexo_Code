Ext.define('Isu.util.FormErrorMessage', {
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
        me.removeAll(true);
        me.add([
            {
                xtype: 'box',
                height: 22,
                width: 26,
                cls: me.errorIcon
            },
            {
                ui: 'form-error',
                name: 'errormsgpanel',
                html: me.text
            }
        ]);
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});
