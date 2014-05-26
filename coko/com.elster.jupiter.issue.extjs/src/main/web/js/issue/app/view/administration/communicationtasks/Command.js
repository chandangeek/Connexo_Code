Ext.define('Isu.view.administration.communicationtasks.Command', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.administration.communicationtasks.CategoryCombo',
        'Isu.view.administration.communicationtasks.ActionCombo',
        'Isu.view.administration.communicationtasks.parameters.clock.Set',
        'Isu.view.administration.communicationtasks.parameters.clock.Synchronize'
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
            ui: 'delete',
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