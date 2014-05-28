Ext.define('Isu.view.workspace.issues.comment.AddForm', {
    extend: 'Ext.form.Panel',
    title: 'Add comment',
    alias: 'widget.comment-add-form',
    layout: 'fit',
    items: {
        itemId: '#Comment',
        xtype: 'textareafield',
        label: 'comment',
        name: 'comment'
    },
    buttons: [
        {   itemId: '#Add',
            text: 'Add',
            action: 'send',
            disabled: true
        },
        {
            itemId: '#Cancel',
            text: 'Cancel',
            action: 'cancel'
        }
    ]
});