Ext.define('Isu.view.administration.communicationtasks.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Isu.view.administration.communicationtasks.Command'
    ],
    alias: 'widget.communication-tasks-edit',
    content: [
        {
            xtype: 'form',
            ui: 'large',
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name',
                    labelWidth: 200,
                    labelSeparator: ' *',
                    allowBlank: false
                },
                {
                    layout: 'column',
                    items: [
                        {
                            xtype: 'label',
                            html: 'Commands*',
                            width: 200,
                            cls: 'x-form-item-label uni-form-item-bold x-form-item-label-right'
                        },
                        {
                            xtype: 'container',
                            name: 'commandnames'
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