Ext.define('Isu.view.workspace.issues.comment.AddForm', {
    extend: 'Ext.form.Panel',
    title: 'Add comment',
    alias: 'widget.comment-add-form',
    layout: 'fit',
    items: {
        itemId: 'comment-area',
        xtype: 'textareafield',
        label: 'comment',
        name: 'comment'
    },
    buttons: [
        {   itemId: 'add',
            text: 'Add',
            action: 'send',
            disabled: true
        },
        {
            itemId: 'cancel',
            text: 'Cancel',
            action: 'cancel'
        }
    ]
});