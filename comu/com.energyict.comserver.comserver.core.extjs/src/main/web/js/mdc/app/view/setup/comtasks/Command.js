Ext.define('Mdc.view.setup.comtasks.Command', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.view.setup.comtasks.CategoryCombo',
        'Mdc.view.setup.comtasks.ActionCombo',
        'Mdc.view.setup.comtasks.parameters.clock.Set',
        'Mdc.view.setup.comtasks.parameters.clock.Synchronize'
    ],
    alias: 'widget.communication-tasks-command',
    border: false,
    items: [
        {
            xtype: 'communication-tasks-categorycombo'
        }
    ],
    margin: '5 0 10 100',
    bbar: [
        {
            xtype: 'button',
            text: 'Add',
            ui: 'action',
            action: 'addCommand',
            disabled: true,
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Save',
            ui: 'action',
            action: 'saveCommand',
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Remove',
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