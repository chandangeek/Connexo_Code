Ext.define('Isu.view.administration.communicationtasks.Form', {
    extend: 'Ext.form.Panel',
    alias: 'widget.communication-tasks-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 1
    },
    ui: 'medium',

    items: [
        {
            xtype: 'displayfield',
            fieldLabel: 'Name',
            name: 'name'
        },
        {
            xtype: 'displayfield',
            fieldLabel: 'Commands',
            name: 'commands'
        }
    ]
});