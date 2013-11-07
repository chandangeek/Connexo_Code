Ext.define('Mdc.view.setup.ComServerEdit', {
    extend: 'Ext.container.Container',
    alias: 'widget.comServerEdit',

    title: 'Edit ComServer',
    layout: 'fit',
    autoShow: true,

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

//        this.buttons = [
//            {
//                text: 'Save',
//                action: 'save'
//            },
//            {
//                text: 'Cancel',
//                scope: this,
//                handler: this.close
//            }
//        ];

        this.callParent(arguments);
    }
});
