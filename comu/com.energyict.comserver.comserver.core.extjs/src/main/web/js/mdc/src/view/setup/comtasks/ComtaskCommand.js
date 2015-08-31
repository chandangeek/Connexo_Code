Ext.define('Mdc.view.setup.comtasks.ComtaskCommand', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo',
        'Mdc.view.setup.comtasks.ComtaskCommandCategoryActionCombo',
        'Mdc.view.setup.comtasks.parameters.clock.Set',
        'Mdc.view.setup.comtasks.parameters.clock.Synchronize'
    ],
    alias: 'widget.comtaskCommand',
    border: false,
    items: [
        {
            xtype: 'comtaskCommandCategoryCombo',
            itemId: 'command-category-combo'
        }
    ],
    margin: '5 0 10 100',
    bbar: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.add','MDC','Add'),
            ui: 'action',
            action: 'addCommand',
            disabled: true,
            hidden: true
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.save','MDC','Save'),
            ui: 'action',
            action: 'saveCommand',
            hidden: true
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.remove','MDC','Remove'),
            ui: 'remove',
            action: 'removeCommand',
            hidden: true
        },
        {
            xtype: 'button',
            ui: 'link',
            text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
            action: 'cancelEditCommand',
            hidden: true
        }
    ]
});