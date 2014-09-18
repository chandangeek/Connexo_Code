Ext.define('Mdc.view.setup.comtasks.ComtaskCreateEditForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.comtaskCreateEdit',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskCommand',
        'Uni.view.grid.ConnectedGrid'
    ],

    content: [
        {
            xtype: 'form',
            ui: 'large',
            defaults: {
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'errors',
                    hidden: true
                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                    required: true,
                    width: 900
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
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
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('comtask.messages', 'MDC', 'Messages'),
                    items:[
                        {
                            xtype: 'displayfield',
                            value: Uni.I18n.translate('comtask.messages.text', 'MDC', 'Send pending messages of these message categories every time this communication task executes')
                        },
                        {
                            xtype: 'connected-grid',
                            itemId: 'messagesConnectedGrid',
                            allItemsTitle: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Message categories'),
                            allItemsStoreName: 'Mdc.store.MessageCategories',
                            selectedItemsTitle: Uni.I18n.translate('comtask.selected.message.categories', 'MDC', 'Selected message categories'),
                            selectedItemsStoreName: 'Mdc.store.SelectedMessageCategories',
                            displayedColumn: 'name'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    margin: '-25 0 0 215',
                    itemId: 'protocolTasksErrorMessage',
                    hidden: true
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