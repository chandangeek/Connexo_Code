/**
 * @class Uni.view.window.Confirmation
 */
Ext.define('Uni.view.window.Confirmation', {
    extend: 'Ext.window.MessageBox',
    xtype: 'confirmation-window',

    /**
     * @cfg {String}
     *
     * Text for the confirmation button. By default 'Remove'.
     */
    confirmText: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),

    /**
     * @cfg {String}
     *
     * Text for the cancellation button. By default 'Cancel'.
     */
    cancelText: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),

    /**
     * @cfg {Function}
     *
     * Callback to call if the user chooses to confirm. By default it hides the window.
     */
    confirmation: function () {
        var btn = this.header.child('[type=close]');
        // Give a temporary itemId so it can act like a confirm button.
        btn.itemId = 'confirm';
        this.btnCallback(btn);
        delete btn.itemId;
        this.hide();
    },

    /**
     * @cfg {Function}
     *
     * Callback to call if the user chooses to cancel. By default it closes the window.
     */
    cancellation: function () {
        this.close();
    },

    initComponent: function () {
        var me = this;

        me.buttons = [
            {
                xtype: 'button',
                action: 'confirm',
                name: 'confirm',
                scope: me,
                text: me.confirmText,
                ui: 'remove',
                handler: me.confirmation
            },
            {
                xtype: 'button',
                action: 'cancel',
                name: 'cancel',
                scope: me,
                text: me.cancelText,
                ui: 'link',
                handler: me.cancellation
            }
        ];

        me.callParent(arguments);
    },

    show: function (config) {
        Ext.apply(config, {
            icon: Ext.MessageBox.WARNING
        });

        this.callParent(arguments);
    }
});