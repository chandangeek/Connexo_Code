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
                    labelWidth: 200,
                    required: true,
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
                            html: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands') + ' *',
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