Ext.define('Isu.view.issues.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.bulk-step2',
    title: Uni.I18n.translate('issue.selectAction','ISU','Select action'),

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
                        { itemId: 'Assign',
                            boxLabel: 'Assign issues',
                            name: 'operation',
                            inputValue: 'assign',
                            checked: true,
                            privileges: Isu.privileges.Issue.assign
                        },
                        { itemId: 'Close',
                            boxLabel: 'Close issues',
                            name: 'operation',
                            inputValue: 'close',
                            privileges: Isu.privileges.Issue.close
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});