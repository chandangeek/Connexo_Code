Ext.define('Mtr.view.workspace.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: 'Select action',
    border: false,

    requires: [
        'Ext.form.RadioGroup'
    ],

    items: [
        {
            xtype: 'panel',
            border: false,

            items: [
                {
                    xtype: 'radiogroup',
                    columns: 1,
                    vertical: true,
                    defaults: {
                        name: 'operation',
                        submitValue: false
                    },

                    items: [
                        { boxLabel: 'Assign issues', name: 'operation', inputValue: 'assign', checked: true },
                        { boxLabel: 'Close issues', name: 'operation', inputValue: 'close'}
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});