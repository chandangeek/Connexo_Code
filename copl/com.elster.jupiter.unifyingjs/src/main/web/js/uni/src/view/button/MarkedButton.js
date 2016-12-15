Ext.define('Uni.view.button.MarkedButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.marked-button',
    ui: 'plain',
    style: 'font-size: 20px',
    enableToggle: true,
    markedCls: 'icon-star-full',
    unmarkedCls: 'icon-star-empty',
    hidden: true,
    pressed: false,
    record: null,
    popUpConfig: null,
    markedTooltip: Uni.I18n.translate('general.unmark', 'UNI', 'Unmark'),
    unmarkedTooltip: Uni.I18n.translate('general.mark', 'UNI', 'Mark'),

    toggleHandler: function (button, pressed) {
        var me = this;

        Ext.suspendLayouts();
        me.setIconCls(pressed ? me.markedCls : me.unmarkedCls);
        me.setTooltip(pressed ? me.markedTooltip : me.unmarkedTooltip);
        Ext.resumeLayouts(true);

        if (pressed) {
            me.mark();
            me.showCommentPopUp();
        } else {
            me.unmark();
        }
    },

    initComponent: function () {
        var me = this;

        me.popUp = Ext.widget('window', Ext.apply({
            title: Uni.I18n.translate('general.addComment', 'UNI', 'Add comment'),
            itemId: 'marked-window',
            closable: false,
            closeAction: 'hide',
            alignTarget: me,
            defaultAlign: 'tl-br',
            items: {
                xtype: 'form',
                itemId: 'marked-form',
                defaults: {
                    labelWidth: 100,
                    width: 373
                },
                items: [
                    {
                        xtype: 'textareafield',
                        itemId: 'marked-comment',
                        name: 'comment',
                        fieldLabel: Uni.I18n.translate('general.comment', 'UNI', 'Comment'),
                        height: 128
                    }
                ],
                buttons: [
                    {
                        xtype: 'button',
                        itemId: 'add-comment',
                        text: Uni.I18n.translate('general.addComment', 'UNI', 'Add comment'),
                        ui: 'action',
                        handler: function () {
                            me.popUp.down('form').updateRecord(me.record);
                            me.mark();
                            me.popUp.close();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancel-add-comment',
                        ui: 'link',
                        text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                        handler: function () {
                            me.popUp.close();
                        }
                    }
                ]
            }
        }, Ext.isObject(me.popUpConfig) ? me.popUpConfig : {}));

        me.callParent(arguments);
        Ext.ModelManager.getModel(me.record.$className).load(me.record.getId(), {
            scope: me,
            callback: me.onLoad
        });
        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    onBeforeDestroy: function () {
        var me = this;
        me.popUp.destroy();
    },

    onLoad: function (record) {
        var me = this,
            isFavorite = record.get('favorite');

        Ext.suspendLayouts();
        me.record = record;
        me.show();
        me.toggle(isFavorite, true);
        me.setIconCls(isFavorite ? me.markedCls : me.unmarkedCls);
        me.setTooltip(isFavorite ? me.markedTooltip : me.unmarkedTooltip);
        Ext.resumeLayouts(true);
    },

    mark: function () {
        var me = this;

        me.record.save({
            scope: me,
            isNotEdit: true,
            callback: me.onLoad
        });
    },

    unmark: function () {
        var me = this;

        me.popUp.close();
        me.record.destroy({
            scope: me,
            isNotEdit: true,
            callback: me.onLoad
        });
    },

    showCommentPopUp: function () {
        var me = this;

        Ext.suspendLayouts();
        me.popUp.show();
        me.popUp.down('form').loadRecord(me.record);
        Ext.resumeLayouts(true);
    }
});