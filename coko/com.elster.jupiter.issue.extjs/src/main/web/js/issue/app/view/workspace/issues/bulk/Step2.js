Ext.define('Mtr.view.workspace.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: 'Select action',

    requires: [
        'Ext.form.RadioGroup'
    ],

    items: [
        {
            xtype: 'panel',
            border: 0,

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
                        { boxLabel: 'Assign issues', checked: true, inputValue: 'assign' },
                        { boxLabel: 'Close issues', inputValue: 'close' }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});