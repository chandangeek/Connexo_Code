/**
 * @class Uni.view.window.Acknowledgement
 */
Ext.define('Uni.view.window.Acknowledgement', {
    extend: 'Ext.window.Window',
    xtype: 'acknowledgement-window',

    autoShow: true,
    resizable: false,
    bodyBorder: false,
    shadow: false,
    animCollapse: true,
    border: false,
    header: false,
    cls: Uni.About.baseCssPrefix + 'window-acknowledgement',

    layout: {
        type: 'hbox',
        align: 'center'
    },

    setMessage: function (message) {
        var msgPanel = this.down('#msgmessage');

        Ext.suspendLayouts();

        msgPanel.removeAll();
        msgPanel.add({
            xtype: 'label',
            html: Ext.String.htmlEncode(message)
        });

        Ext.resumeLayouts();
    },

    initComponent: function () {
        var me = this;

        me.items = [
            // Icon.
            {
                xtype: 'component',
                cls: 'icon'
            },
            // Message.
            {
                xtype: 'panel',
                itemId: 'msgmessage',
                cls: 'message',
                layout: {
                    type: 'vbox',
                    align: 'left'
                }
            },
            {
                xtype: 'component',
                html: '&nbsp;',
                flex: 1
            },
            // Close button.
            {
                xtype: 'button',
                iconCls: 'close',
                ui: 'close',
                width: 28,
                height: 28,
                handler: function () {
                    me.close();
                }
            }
        ];

        me.callParent(arguments);
    }
});
