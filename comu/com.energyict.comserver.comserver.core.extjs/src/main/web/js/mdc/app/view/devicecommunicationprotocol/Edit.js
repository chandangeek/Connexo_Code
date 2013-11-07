Ext.define('Mdc.view.devicecommunicationprotocol.Edit', {
    extend: 'Ext.window.Window',
    alias: 'widget.deviceCommunicationProtocolEdit',
    title: 'Edit Device Communication Protocol',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    width: 600,
    height: 450,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*'
        //'Ext.util.Point' // Required for the drag and drop.
    ],

    initComponent: function () {
        this.buttons = [
            {
                text: 'Clone',
                action: 'clone'
            },
            {
                text: 'Save',
                action: 'save'
            },
            {
                text: 'Cancel',
                scope: this,
                handler: this.close
            }
        ];

        var columns = [
            {
                text: 'Name',
                flex: 1,
                dataIndex: 'name'
            }
        ];


        this.items = [
            {
                xtype: 'form',
                border: false,
                padding: '10 10 0 10',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    anchor: '100%',
                    margins: '0 0 5 0'
                },

                items: [
                    {
                        xtype: 'textfield',
                        name: 'databaseId',
                        fieldLabel: 'Id'
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        fieldLabel: 'Name'
                    },
                    {
                        xtype: 'textfield',
                        name: 'javaClassName',
                        fieldLabel: 'JavaClassName'
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

