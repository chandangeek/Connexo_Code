/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.window.Confirmation
 */
Ext.define('Uni.view.window.Confirmation', {
    extend: 'Ext.window.MessageBox',
    xtype: 'confirmation-window',

    /**
     * @cfg {Boolean}
     *
     * Indicates if you want the icon, the title and the confirmation button in green.
     * By default false.
     */
    green: false,

    /**
     * @cfg {Boolean}
     *
     * Indicates if you want the icon, the title and the confirmation button in orange.
     * By default false.
     */
    orange: false,

    noConfirmBtn: false,
    cls: Uni.About.baseCssPrefix + 'confirmation-window',
    confirmBtnUi: 'remove',
    /**
     * @cfg {String}
     *
     * Text for the confirmation button. By default 'Remove'.
     */
    confirmText: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),

    /**
     * @cfg {String}
     *
     * Text for a 2nd confirmation button. If defined, this indicates you want a 2nd confirm button
     */
    secondConfirmText: undefined,

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

        if (me.green) {
            me.cls = Uni.About.baseCssPrefix + 'confirmation-window-green';
            me.confirmBtnUi = 'action';
        } else if (me.orange) {
            me.cls = Uni.About.baseCssPrefix + 'confirmation-window-orange';
            me.confirmBtnUi = 'confirm';
        }
        me.callParent(arguments);

        if (Ext.isDefined(me.secondConfirmText)) {
            me.add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'button',
                            action: 'confirm',
                            name: 'confirm',
                            scope: me,
                            text: me.confirmText,
                            ui: me.confirmBtnUi,
                            handler: me.confirmation,
                            margin: '0 0 0 ' + me.iconWidth,
                            itemId: 'confirm-button'
                        },
                        {
                            xtype: 'button',
                            action: 'confirm2',
                            name: 'confirm2',
                            scope: me,
                            text: me.secondConfirmText,
                            ui: me.confirmBtnUi,
                            handler: me.confirmation,
                            margin: '0 0 0 7',
                            itemId: 'confirm2-button'
                        },
                        {
                            xtype: 'button',
                            action: 'cancel',
                            name: 'cancel',
                            scope: me,
                            text: me.cancelText,
                            ui: 'link',
                            handler: me.cancellation,
                            itemId: 'cancel-button'
                        }
                    ]
                }
            );
        } else if (me.noConfirmBtn) {
            me.add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'button',
                            action: 'cancel',
                            name: 'cancel',
                            scope: me,
                            text: me.cancelText,
                            ui: 'link',
                            margin: '0 0 0 ' + (me.iconWidth - 7),
                            handler: me.cancellation,
                            itemId: 'cancel-button'
                        }
                    ]
                }
            );
        } else {
            me.add(
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox'
                    },
                    items: [
                        {
                            xtype: 'button',
                            action: 'confirm',
                            name: 'confirm',
                            scope: me,
                            text: me.confirmText,
                            ui: me.confirmBtnUi,
                            handler: me.confirmation,
                            margin: '0 0 0 ' + me.iconWidth,
                            itemId: 'confirm-button'
                        },
                        {
                            xtype: 'button',
                            action: 'cancel',
                            name: 'cancel',
                            scope: me,
                            text: me.cancelText,
                            ui: 'link',
                            handler: me.cancellation,
                            itemId: 'cancel-button'
                        }
                    ]
                }
            );
        }
    },

    show: function (config) {
        var me = this;
        
        if (Ext.isDefined(config.htmlEncode)) {
            me.msg.htmlEncode = config.htmlEncode;
        }

        if (!Ext.isDefined(config.icon)) {
            Ext.apply(config, {
                icon: me.green ? 'icon-info' : (me.orange ? 'icon-question' : 'icon-warning')
            });
        }
        if (!Ext.isDefined(config.closable)) {
            Ext.apply(config, {
                closable: true
            });
        }

        this.callParent(arguments);
    }
});
