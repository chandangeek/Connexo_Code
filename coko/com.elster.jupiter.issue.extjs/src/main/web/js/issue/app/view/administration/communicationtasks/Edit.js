Ext.define('Isu.view.administration.communicationtasks.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.communicationtasks.CategoryCombo'
    ],
    alias: 'widget.communication-tasks-edit',
    content: [
        {
            ui: 'large',
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name',
                    labelSeparator: ' *',
                    allowBlank: false
                },
                {
                    layout: 'column',
                    items: [
                        {
                            xtype: 'label',
                            html: 'Commands*'
                        },
                        {
                            xtype: 'container',
                            name: 'commandname'
                        },
                        {
                            xtype: 'container',
                            name: 'commandfields'
                        }
                    ]
                }
            ]
        }
    ]
});