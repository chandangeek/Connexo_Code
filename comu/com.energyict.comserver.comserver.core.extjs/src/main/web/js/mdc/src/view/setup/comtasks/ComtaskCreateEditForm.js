Ext.define('Mdc.view.setup.comtasks.ComtaskCreateEditForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskCreateEdit',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskCommand'
    ],
    content: [
        {
            xtype: 'form',
            ui: 'large',
            items: [
                {
                    itemId: 'errors',
                    name: 'errors',
                    layout: 'hbox',
                    margin: '0 0 20 100',
                    hidden: true,
                    defaults: {
                        xtype: 'container',
                        cls: 'isu-error-panel'
                    }
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                    required: true,
                    allowBlank: false,
                    labelWidth: 200,
                    width: 900
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                    labelWidth: 200,
                    required: true,
                    items: [
                        {
                          xtype: 'container',
                          itemId: 'buttonsAndNamesContainer',
                            items: [
                                {
                                    xtype: 'button',
                                    name: 'addCommands',
                                    text: 'Add Commands',
                                    margin: '5 0 5 0',
                                    itemId: 'addCommandsToTask',
                                    action: 'addCommand'
                                },
                                {
                                    xtype: 'container',
                                    layout: {
                                        type: 'vbox'
                                    },
                                    name: 'commandnames'
                                },
                                {
                                    xtype: 'button',
                                    itemId: 'addAnotherCommandsButton',
                                    text: Uni.I18n.translate('comtask.add.another', 'MDC', '+ Add another'),
                                    margin: '5 0 5 0',
                                    action: 'addMoreCommands',
                                    hidden: true
                                }

                            ]
                        }
                    ]
                },
                {
                    xtype: 'toolbar',
                    margin: '0 0 0 215',
                    items: [
                        {
                            xtype: 'button',
                            name: 'action',
                            ui: 'action',
                            itemId: 'createEditTask'
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