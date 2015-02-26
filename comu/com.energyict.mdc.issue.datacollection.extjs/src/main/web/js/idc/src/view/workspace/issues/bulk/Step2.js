Ext.define('Idc.view.workspace.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: 'Select action',

    requires: [
        'Ext.form.RadioGroup'
    ],

    items: [
        {
            xtype: 'panel',
            border: false,

            items: [
                {
                    itemId: 'radiogroupStep2',
                    xtype: 'radiogroup',
                    columns: 1,
                    vertical: true,
                    defaults: {
                        name: 'operation',
                        submitValue: false
                    },

                    items: [
                        { itemId: 'Assign', boxLabel: 'Assign issues', name: 'operation', inputValue: 'assign', checked: true, hidden: Uni.Auth.hasNoPrivilege('privilege.assign.issue') },
                        { itemId: 'Close', boxLabel: 'Close issues', name: 'operation', inputValue: 'close', hidden: Uni.Auth.hasNoPrivilege('privilege.close.issue')}
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});