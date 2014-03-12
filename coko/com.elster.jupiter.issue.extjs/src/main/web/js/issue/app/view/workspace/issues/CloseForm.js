Ext.define('Isu.view.workspace.issues.CloseForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.TextArea'
    ],
    alias: 'widget.issues-close-form',

    defaults: {
        border: false
    },

    items: [
        {
            xype: 'container',
            border: 0,
            margin: '0 0 0 -25',
            items: [
                {
                    xtype: 'radiogroup',
                    fieldLabel: 'Reason *',
                    name: 'status',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    items: [
                        { boxLabel: 'Resolved', name: 'status', inputValue: 'CLOSED', checked: true },
                        { boxLabel: 'Rejected', name: 'status', inputValue: 'REJECTED' }
                    ]
                },
                {
                    xtype: 'textarea',
                    fieldLabel: 'Comment',
                    name: 'comment',
                    width: 500,
                    height: 150,
                    emptyText: 'Provide a comment (optionally)'
                }
            ]
        }
    ]
});