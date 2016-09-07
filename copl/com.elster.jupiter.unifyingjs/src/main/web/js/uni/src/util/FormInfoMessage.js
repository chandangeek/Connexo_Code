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

    beforeRender: function () {
        var me = this;
        me.renew();
        me.callParent(arguments)
    },

    initComponent: function() {
        var me = this;

        if (Ext.isEmpty(me.iconCmp)) {
            me.lbar = {
                padding: '5 0 0 0',
                items: {
                    xtype: 'box',
                    height: 22,
                    width: 26,
                    style: {
                        fontSize: '22px',
                        color: '#71adc7'
                    },
                    cls: 'icon-info'
                }
            }
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