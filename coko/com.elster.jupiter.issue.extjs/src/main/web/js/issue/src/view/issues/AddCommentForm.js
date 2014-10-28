Ext.define('Isu.view.issues.AddCommentForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-add-comment-form',
    layout: 'fit',
    items: {
        itemId: 'issue-add-comment-area',
        xtype: 'textareafield',
        height: 100,
        fieldLabel: 'Comment',
        labelAlign: 'top',
        name: 'comment'
    },

    bbar: {
        layout: {
            type: 'hbox',
            align: 'left'
        },
        items: [
            {
                itemId: 'issue-comment-save-button',
                text: 'Add',
                ui: 'action',
                action: 'send',
                disabled: true
            },
            {
                itemId: 'issue-comment-cancel-adding-button',
                text: 'Cancel',
                action: 'cancel',
                ui: 'link'
            }
        ]
    }
});