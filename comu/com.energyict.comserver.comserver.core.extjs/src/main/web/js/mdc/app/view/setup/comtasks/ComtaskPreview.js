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
        xtype: 'panel',
        itemId: 'comtaskPreviewFieldsPanel',
        layout: 'column',
        ui: 'medium',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 1
        },

        items: [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                itemId: 'tasksName',
                name: 'name'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                itemId: 'tasksCommands',
                name: 'commandsString'
            }
        ]
    }
});