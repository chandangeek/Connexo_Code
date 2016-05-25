Ext.define('Uni.util.FormInfoMessage', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.uni-form-info-message',
    cls: Uni.About.baseCssPrefix + 'panel-no-items-found',
    ui: 'small',
    framed: true,
    text: null,
    layout: {
        type: 'hbox',
        align: 'middle'
    },
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

    renew: function () {
        var me = this;

        Ext.suspendLayouts();

        me.removeAll(true);

        if (Ext.isEmpty(me.iconCmp)) {
            me.iconCmp = {
                xtype: 'box',
                height: 22,
                width: 26,
                style: {
                    fontSize: '22px',
                    color: '#71adc7'
                },
                cls: 'icon-info'
            };
        }
        me.add([
            me.iconCmp,
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