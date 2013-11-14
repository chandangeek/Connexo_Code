Ext.define('Mdc.view.setup.devicecommunicationprotocol.Edit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    autoShow: true,
    border: 0,
    autoWidth: true,

    initComponent: function () {
        this.items = [
            {
                xtype: 'form',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [
                    {
                        xtype: 'fieldset',
                        title: 'Required',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'id',
                                fieldLabel: 'Id',
                                id: 'deviceCommunicationProtocolId',
                                readOnly: true,
                                autoWidth: true,
                                size:10,
                                hidden: true

                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: 'Name',
                                autoWidth: true,
                                size:50
                            },
                            {
                                xtype: 'textfield',
                                name: 'javaClassName',
                                fieldLabel: 'Java class name',
                                autoWidth: true,
                                size:75
                            }
                        ]
                    }
                ]

            }
        ]
        ;

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
})
;