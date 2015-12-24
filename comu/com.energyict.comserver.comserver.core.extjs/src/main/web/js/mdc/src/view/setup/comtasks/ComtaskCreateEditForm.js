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
                    hidden: true,
                    width: 380

                },
                {
                    xtype: 'textfield',
                    name: 'name',
                    itemId: 'addComtaskName',
                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                    required: true,
                    width: 500,
                    listeners: {
                        afterrender: function(field) {
                            field.focus(false, 200);
                        }
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('comtask.actions', 'MDC', 'Actions'),
                    items: [
                        {
                          xtype: 'container',
                          itemId: 'buttonsAndNamesContainer',
                            items: [
                                {
                                    xtype: 'container',
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: Uni.I18n.translate('general.noActionsAdded', 'MDC', 'No actions have been added'),
                                            itemId: 'noActionsAddedMsg',
                                            style: {
                                                'font': 'italic 13px/17px Lato',
                                                'color': '#686868',
                                                'margin-top': '6px',
                                                'margin-right': '10px'
                                            }
                                        },
                                        {
                                            xtype: 'button',
                                            name: 'addCommands',
                                            text: Uni.I18n.translate('general.addAction', 'MDC', 'Add action'),
                                            margin: '0 0 0 5',
                                            itemId: 'addCommandsToTask',
                                            action: 'addCommand'
                                        }
                                    ]
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
                                    text: Uni.I18n.translate('comtask.add.another', 'MDC', 'Add another'),
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
                    fieldLabel: Uni.I18n.translate('comtask.messages', 'MDC', 'Commands'),
                    items:[
                        {
                            xtype: 'displayfield',
                            value: Uni.I18n.translate('comtask.messages.text', 'MDC', 'Send pending commands of these command categories every time this communication task executes')
                        },
                        {
                            xtype: 'connected-grid',
                            itemId: 'messagesConnectedGrid',
                            disableIndication: true,
                            enableSorting: true,
                            allItemsTitle: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Command categories'),
                            allItemsStoreName: 'Mdc.store.MessageCategories',
                            selectedItemsTitle: Uni.I18n.translate('comtask.selected.message.categories', 'MDC', 'Selected command categories'),
                            selectedItemsStoreName: 'Mdc.store.SelectedMessageCategories',
                            displayedColumn: 'name'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    margin: '-25 0 30 215',
                    itemId: 'protocolTasksErrorMessage',
                    hidden: true
                },
                {
                    xtype: 'toolbar',
                    margin: '-30 0 0 215',
                    items: [
                        {
                            xtype: 'button',
                            name: 'action',
                            ui: 'action',
                            itemId: 'createEditTask'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancelLink',
                            text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
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
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        // To remove the horizontal scroll bars of the categories grids
        me.on('afterrender', function() {
            me.down('#messagesConnectedGrid #allItemsGrid').view.el.dom.style.overflowX = 'hidden';
            me.down('#messagesConnectedGrid #selectedItemsGrid').view.el.dom.style.overflowX = 'hidden';
        }, me, {single:true});
    }
});