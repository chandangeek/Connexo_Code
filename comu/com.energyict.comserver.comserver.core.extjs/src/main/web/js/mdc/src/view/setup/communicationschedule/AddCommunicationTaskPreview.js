Ext.define('Mdc.view.setup.communicationschedule.AddCommunicationTaskPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.addCommunicationTaskPreview',
    itemId: 'addCommunicationTaskPreview',
    hidden: false,
    title: 'Details',
    frame: true,
    items: {
        xtype: 'panel',
        itemId: 'AddCommunicationTaskPreviewFieldsPanel',
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
                itemId: 'comtaskCommands',
                fieldLabel: Uni.I18n.translate('comtask.commands', 'MDC', 'Commands'),
                name: 'commandsString'
            }
        ]
    }
});