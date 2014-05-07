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
    bbar: [
        {
            xtype: 'button',
            text: 'Add',
            action: 'addCommand',
            disabled: true,
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Save',
            action: 'saveCommand',
            hidden: true
        },
        {
            xtype: 'button',
            text: 'Remove',
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
    ],

    loadCommand: function (command) {

    },

    getCommand: function () {
        var command;

        return command;
    }
});