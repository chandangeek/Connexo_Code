Ext.define('Mdc.view.setup.ComServerEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.comServerEdit',

    layout: 'fit',
    autoShow: true,
    border: 0,

    initComponent: function() {
        this.items = [
            {
                xtype: 'form',
                items: [
                    {
                        xtype: 'textfield',
                        name : 'name',
                        fieldLabel: 'Name'
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
