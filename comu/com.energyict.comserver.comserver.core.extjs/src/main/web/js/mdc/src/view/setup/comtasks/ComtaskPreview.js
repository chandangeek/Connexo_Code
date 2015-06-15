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
            privileges: Mdc.privileges.Communication.admin,
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
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Actions'),
                name: 'commands',
                renderer: function (value) {
                    var str = '';
                    if (value) {
                        Ext.Array.each(value, function (command) {
                            var translationKey = 'comtask.action.' + command.category + '.' + command.action;
                            str += Uni.I18n.translate(translationKey, 'MDC', command.category + ' - ' + command.action) + '<br />';
                        });
                    }
                    return str;
                }
            },
            {
                itemId: 'comtaskMessages',
                fieldLabel: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Command categories'),
                name: 'messages',
                renderer: function (value) {
                    var str = '';
                    if (value) {
                        Ext.Array.each(value, function (message) {
                            str += Ext.String.htmlEncode(message.name) + '<br />';
                        });
                    }
                    return str;
                }
            }
        ]
    }
});