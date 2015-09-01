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
                text: Uni.I18n.translate('general.add','ISU','Add'),
                ui: 'action',
                action: 'send',
                disabled: true
            },
            {
                itemId: 'issue-comment-cancel-adding-button',
                text: Uni.I18n.translate('general.cancel','ISU','Cancel'),
                action: 'cancel',
                ui: 'link'
            }
        ]
    }
});