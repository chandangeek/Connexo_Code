Ext.define('Mdc.view.setup.comtasks.ComtaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comtaskPreview',
    hidden: false,
    title: 'Details',
    frame: true,
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionMenu'
    ],
    tools: [
        {
            xtype: 'button',
            text: 'Actions',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'comtaskActionMenu'
            }
        }
    ],
    items: {
        xtype: 'form',
        border: false,
        itemId: 'comtaskPreviewFieldsPanel',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },

        defaults: {
            xtype: 'displayfield',
            labelWidth: 200
        },

        items: [
            {
                itemId: 'comtaskName',
                fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                name: 'name'
            },
            {
                itemId: 'comtaskCommands',
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                name: 'commands',
                renderer: function (value) {
                    var str = '';
                    if (value) {
                        Ext.Array.each(value, function (command) {
                            str += command.action.charAt(0).toUpperCase() + command.action.slice(1) + ' ' + command.category.charAt(0).toUpperCase() + command.category.slice(1) + '<br />';
                        });
                    }
                    return str;
                }
            },
            {
                itemId: 'comtaskMessages',
                fieldLabel: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Message categories'),
                name: 'messages',
                renderer: function (value) {
                    var str = '';
                    if (value) {
                        Ext.Array.each(value, function (message) {
                            str += message.name + '<br />';
                        });
                    }
                    return str;
                }
            }
        ]
    }
});