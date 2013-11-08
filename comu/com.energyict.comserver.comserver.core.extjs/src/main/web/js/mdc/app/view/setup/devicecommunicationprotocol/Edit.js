Ext.define('Mdc.view.setup.devicecommunicationprotocol.Edit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolEdit',

    layout: 'fit',
    autoShow: true,
    border: 0,

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                items: [
                    {
                        xtype: 'textfield',
                        name: 'id',
                        fieldLabel: 'Id',
                        readOnly: true

                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'javaClassName',
                        fieldLabel: 'Java class name'
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