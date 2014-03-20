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
            items: [
                {
                    xtype: 'radiogroup',
                    fieldLabel: 'Reason *',
                    name: 'status',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    items: [
                        { boxLabel: 'Resolved', name: 'status', inputValue: 'Resolved', checked: true },
                        { boxLabel: 'Won\'t fix', name: 'status', inputValue: 'Won\'t fix' }
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
    ],

    listeners: {
        afterrender: function (form) {
            var values = Ext.state.Manager.get('formCloseValues');
            if (values) {
                Ext.Object.each(values, function (key, value) {
                    if (key == 'comment') {
                        form.down('textarea').setValue(value);
                    }
                });
            }
            var selRadio = Ext.state.Manager.get('formCloseRadio');
            if (selRadio) {
                var radio = form.down('radiogroup').down('[inputValue=' + selRadio + ']');
                radio.setValue(true);
            }
        }
    }
});