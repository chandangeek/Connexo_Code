Ext.define('Uni.util.FormInfoMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-info-message',
    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
    ui: 'small',
    framed: true,
    text: null,
    layout: 'fit',
    style: {
        borderColor: '#71adc7'
    },
    margin: '7 0 32 0',
    htmlEncode: true,
    iconCmp: null,
    shrinkWrapDock: true,

    beforeRender: function () {
        var me = this;
        me.renew();
        me.callParent(arguments)
    },

    initComponent: function() {
        var me = this;

        me.lbar = me.iconCmp || {
                xtype: 'box',
                height: 45,
                width: 26,
                style: {
                    fontSize: '22px',
                    color: '#71adc7'
                },
                cls: 'icon-info'
            }

        this.callParent(arguments);
    },

    renew: function () {
        var me = this;

        Ext.suspendLayouts();
        me.removeAll(true);
        me.add({
            ui: 'form-error',
            name: 'errormsgpanel',
            html: me.text
        });

        Ext.resumeLayouts();
    },

    setText: function (text) {
        var me = this;
        me.text = text;
        me.renew();
    }

});