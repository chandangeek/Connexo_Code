Ext.define('Isu.view.issues.CloseForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.TextArea'
    ],
    alias: 'widget.issues-close-form',

    items: [
        {
            xype: 'container',
            border: 0,
            items: [
                {
                    itemId: 'radiogroup',
                    xtype: 'radiogroup',
                    fieldLabel: 'Reason *',
                    name: 'status',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    items: []
                },
                {
                    itemId: 'Comment',
                    xtype: 'textarea',
                    fieldLabel: 'Comment',
                    name: 'comment',
                    width: 500,
                    height: 150,
                    emptyText:  Uni.I18n.translate('issues.provideComment','ISU','Provide a comment \r\n(optionally)')
                }
            ]
        }
    ]
});