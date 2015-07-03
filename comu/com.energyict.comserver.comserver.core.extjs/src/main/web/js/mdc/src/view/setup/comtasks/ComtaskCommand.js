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
            text: 'Add action',
            ui: 'action',
            action: 'addCommand',
            disabled: true,
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Save action',
            ui: 'action',
            action: 'saveCommand',
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Remove action',
            ui: 'remove',
            action: 'removeCommand',
            hidden: true
        },
        {
            xtype: 'button',
            ui: 'link',
            text: 'Cancel',
            action: 'cancelEditCommand',
            hidden: true
        }
    ]
});