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
    store: null,
    flagRecord: null,
    getParent: null,
    popUpConfig: null,
    labelId: null,
    markedTooltip: Uni.I18n.translate('general.unmark', 'UNI', 'Unmark'),
    unmarkedTooltip: Uni.I18n.translate('general.mark', 'UNI', 'Mark'),
    mixins: {
        bindable: 'Ext.util.Bindable'
    },

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
                            me.popUp.down('form').updateRecord(me.flagRecord);
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

        me.bindStore(me.store || 'ext-empty-store', true);

        me.on('beforedestroy', me.onBeforeDestroy, me);
    },

    onBeforeDestroy: function () {
        var me = this;

        me.bindStore('ext-empty-store');
        me.popUp.destroy();
    },

    getStoreListeners: function () {
        return {
            load: this.onLoad
        };
    },

    onLoad: function () {
        var me = this;

        Ext.suspendLayouts();
        me.flagRecord = me.store.getById(me.labelId);
        me.show();
        me.toggle(!!me.flagRecord, true);
        me.setIconCls(me.flagRecord ? me.markedCls : me.unmarkedCls);
        me.setTooltip(me.flagRecord ? me.markedTooltip : me.unmarkedTooltip);
        Ext.resumeLayouts(true);
    },

    mark: function () {
        var me = this;

        if (!me.flagRecord) {
            me.flagRecord = me.store.createModel({category: {id: me.labelId}});
        }
        if (Ext.isFunction(me.getParent)) {
            me.flagRecord.set('parent', me.getParent());
        }

        me.flagRecord.save({
            isNotEdit: true,
            callback: function () {
                me.store.load();
            }
        });
    },

    unmark: function () {
        var me = this;

        me.popUp.close();
        me.flagRecord.destroy({
            isNotEdit: true,
            callback: function () {
                me.store.load();
            }
        });
    },

    showCommentPopUp: function () {
        var me = this;

        Ext.suspendLayouts();
        me.popUp.show();
        me.popUp.down('form').loadRecord(me.flagRecord);
        Ext.resumeLayouts(true);
    }
});