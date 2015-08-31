Ext.define('Mdc.view.setup.comtasks.ComTaskAddCommandWindow', {
    extend: 'Ext.window.Window',
    xtype: 'comTaskAddCommandWindow',
    itemId: 'comTaskAddCommandWindow',
    closable: false,
    width: 800,
    height: 400,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    items: {
        xtype: 'form',
        border: false,
        itemId: 'comTaskAddCommandForm',
        items: [
            {
                xtype: 'comtaskCommand'
            }
        ]
    },

    bbar: [
        {
            xtype: 'container',
            flex: 1
        },
        {
            xtype: 'container',
            itemId: 'actionBtnContainer'
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
            action: 'cancel',
            ui: 'link',
            listeners: {
                click: {
                    fn: function () {
                        this.up('#comTaskAddCommandWindow').destroy();
                    }
                }
            }
        }

    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.down('#actionBtnContainer').add(
            {
                xtype: 'button',
                text: me.btnText,
                action: me.btnAction,
                ui: 'action',
                itemId: 'addCommandToTask'
            });
    }
});
