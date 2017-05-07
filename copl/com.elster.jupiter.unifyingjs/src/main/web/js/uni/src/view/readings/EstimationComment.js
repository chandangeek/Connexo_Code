/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.readings.EstimationComment', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.estimation-comment',
    requires: [
        'Uni.store.EstimationComment'
    ],
    keepOriginal: false,

    fieldLabel: Uni.I18n.translate('copyFromReference.estimationComment', 'UNI', 'Estimation comment'),
    layout: 'hbox',
    flex: 1,
    items: [
        {
            xtype: 'combobox',
            itemId: 'estimation-comment-box',
            flex: 1,
            name: 'commentId',
            store: Ext.create('Uni.store.EstimationComment'),
            valueField: 'id',
            displayField: 'comment',
            queryMode: 'local',
            editable: false,
            emptyText: Uni.I18n.translate('estimationComment.selectComment', 'UNI', 'Select a comment..'),
            listeners: {
                afterrender: function () {
                    var me = this,
                        keepComment = {
                        comment: Uni.I18n.translate('estimationComment.keepComment', 'UNI', 'Keep original comment'),
                        id: -1
                        },
                        nullComment = {
                            comment: null,
                            id: 0
                        };
                    me.resetButton = me.nextSibling('#estimation-comment-default-button');
                    me.getStore().load(function () {
                        if (me.up('estimation-comment').keepOriginal) {
                            me.getStore().add(keepComment);
                            me.setValue(-1);
                        }
                        me.getStore().add(nullComment);
                    });
                },
                change: function (combobox) {
                    if (combobox.getValue()) {
                        this.resetButton.setDisabled(false);
                    } else {
                        this.resetButton.setDisabled(true);
                    }
                }
            }
        },
        {
            xtype: 'uni-default-button',
            itemId: 'estimation-comment-default-button',
            hidden: false,
            disabled: true,
            tooltip: Uni.I18n.translate('general.clear', 'UNI', 'Clear'),
            handler: function () {
                this.previousSibling('#estimation-comment-box').reset();
            }

        }
    ]
});