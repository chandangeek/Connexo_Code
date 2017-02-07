/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.CommentsList', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.issues.AddCommentForm',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Isu.privileges.Issue'
    ],
    alias: 'widget.issue-comments',
    ui: 'medium',
    buttonAlign: 'left',
    addCommentPrivileges: Isu.privileges.Issue.comment,
    noProcessText: Uni.I18n.translate('processes.issue.noProcessesStarted', 'ISU', 'No process started yet on this issue'),

    buttons: [
        {
            itemId: 'issue-comments-add-comment-button',
            text: Uni.I18n.translate('general.addComment', 'ISU', 'Add comment'),
            hidden: true,
            action: 'add'
        }
    ],
    noCommentText: Uni.I18n.translate('general.NoCommentsCreatedYet', 'ISU', 'No comments created yet on this issue'),
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'no-items-found-panel',
                itemId: 'no-issue-comments',
                title: Uni.I18n.translate('general.NoCommentsFound', 'ISU', 'No comments found'),
                reasons: [
                    me.noCommentText
                ],
                stepItems: [
                    {
                        itemId: 'empty-message-add-comment-button',
                        text: Uni.I18n.translate('general.addComment', 'ISU', 'Add comment'),
                        action: 'add',
                        privileges: this.addCommentPrivileges,
                    }
                ],
                hidden: true
            },
            {
                xtype: 'dataview',
                itemId: 'issue-comments-view',
                title: Uni.I18n.translate('issue.userImages', 'ISU', 'User Images'),
                itemSelector: 'div.thumb-wrap',
                tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '{[xindex > 1 ? "<hr>" : ""]}',
                    '<p><span class="isu-icon-USER"></span><b>{author.name}</b> ' + Uni.I18n.translate('general.addedcomment.lowercase', 'ISU', 'added a comment') + ' - {[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</p>',
                    '<p><tpl for="splittedComments">',
                    '{.:htmlEncode}</br>',
                    '</tpl></p>',
                    '</tpl>',
                    {
                        formatCreationDate: function (date) {
                            date = Ext.isDate(date) ? date : new Date(date);
                            return Uni.DateTime.formatDateTimeLong(date);
                        }
                    }
                ),
                header: 'Name',
                dataIndex: 'name'
            },
            {
                xtype: 'issue-add-comment-form',
                itemId: 'issue-add-comment-form',
                hidden: true
            }
        ];

        me.callParent(arguments);
    },
});