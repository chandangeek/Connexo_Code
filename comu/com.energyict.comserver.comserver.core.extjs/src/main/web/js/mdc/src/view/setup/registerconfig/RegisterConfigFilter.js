Ext.define('Mdc.view.setup.registerconfig.RegisterConfigFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerConfigFilter',
    title: 'Filter',
    cls: 'filter-form',

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name'
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});