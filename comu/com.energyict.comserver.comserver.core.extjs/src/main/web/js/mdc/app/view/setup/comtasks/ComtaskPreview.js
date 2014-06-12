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
                itemId: 'comtaskName',
                fieldLabel: Uni.I18n.translate('comtask.name', 'MDC', 'Name'),
                name: 'name'
            },
            {
                xtype: 'displayfield',
                itemId: 'comtaskCommands',
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                name: 'commandsString'
            }
        ]
    }
});