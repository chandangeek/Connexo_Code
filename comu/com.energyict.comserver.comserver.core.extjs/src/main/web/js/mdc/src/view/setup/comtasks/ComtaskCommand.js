Ext.define('Mdc.view.setup.comtasks.ComtaskCommand', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskCommandCategoryCombo',
        'Mdc.view.setup.comtasks.ComtaskCommandCategoryActionCombo',
        'Mdc.view.setup.comtasks.parameters.time.Set',
        'Mdc.view.setup.comtasks.parameters.time.Synchronize'
    ],
    alias: 'widget.comtaskCommand',
    border: false,
    items: [
        {
            xtype: 'comtaskCommandCategoryCombo',
            itemId: 'command-category-combo'
        }
    ]
});