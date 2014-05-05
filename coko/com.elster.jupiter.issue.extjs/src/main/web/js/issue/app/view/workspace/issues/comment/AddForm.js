Ext.define('Isu.view.workspace.issues.comment.AddForm', {
    extend: 'Ext.form.Panel',
    title: 'Add comment',
    alias: 'widget.comment-add-form',
    layout: 'fit',
    items: {
        xtype: 'textareafield',
        label: 'comment',
        name: 'comment'
    },
    buttons: [
        {
            text: 'Add',
            action: 'send',
            disabled: true
        },
        {
            text: 'Cancel',
            action: 'cancel'
        }
    ]
});