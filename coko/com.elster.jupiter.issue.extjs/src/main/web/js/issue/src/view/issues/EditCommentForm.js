/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.EditCommentForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-edit-comment-form',
    layout: 'fit',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                itemId: 'issue-edit-comment-area',
                xtype: 'textareafield',
                height: 100,
                fieldLabel: Uni.I18n.translate('general.comment', 'ISU', 'Comment'),
                labelAlign: 'top',
                name: 'comment',
                value: me.comment
            }
        ];
        me.bbar = {
            layout: {
                type: 'hbox',
                align: 'left'
            },
            items: [
                {
                    itemId: 'issue-comment-edit-button',
                    text: Uni.I18n.translate('general.save', 'ISU', 'Save'),
                    ui: 'action',
                    action: 'send',
                    disabled: true,
                    editCommentForm: me,
                    editPanel: me.cntComment
                },
                {
                    itemId: 'issue-comment-cancel-editing-button',
                    text: Uni.I18n.translate('general.cancel', 'ISU', 'Cancel'),
                    action: 'cancel',
                    ui: 'link',
                    editCommentForm: me,
                    commentPanel: me.cntComment
                }
            ]
        };
        me.callParent(arguments);
    },


    setComment: function (comment) {
        this.down('#issue-edit-comment-area').setValue(comment);
    }
});