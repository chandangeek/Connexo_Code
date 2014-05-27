Ext.define('Mdc.view.setup.comtasks.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Mdc.view.setup.comtasks.Command'
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
                    allowBlank: false,
                    margin: '0 0 10 -100',
                    labelPad: 100,
                    width: 600
                },
                {
                    layout: 'column',
                    items: [
                        {
                            xtype: 'label',
                            html: 'Commands*',
                            width: 200,
                            margin: '10 0 0 5'
                        },
                        {
                            xtype: 'container',
                            layout: {
                                type: 'vbox'
                            },
                            name: 'commandnames'
                        },
                        {
                            xtype: 'container',
                            margin: '0 0 0 100',
                            name: 'commandfields'
                        }
                    ]
                },
                {
                    xtype: 'toolbar',
                    margin: '0 0 0 190',
                    items: [
                        {
                            xtype: 'button',
                            name: 'action',
                            ui: 'action',
                            formBind: true,
                            itemId: 'createEditTask',
                            disabled: true
                        },
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            action: 'cancel',
                            ui: 'link',
                            listeners: {
                                click: {
                                    fn: function () {
                                        window.location.href = '#/administration/communicationtasks';
                                    }
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ]
});