Ext.define('Mdc.view.setup.devicecommunicationprotocol.Edit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommunicationProtocolEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    itemId: 'deviceCommunicationProtocolEdit',
    autoShow: true,
    border: 0,
    autoWidth: true,
    requires: [
        'Mdc.store.LicensedProtocols',
        'Mdc.view.setup.protocolfamily.List',
        'Mdc.view.setup.property.Edit'
    ],
    initComponent: function () {
        var licensedProtocols = Ext.create('Mdc.store.LicensedProtocols');
        this.items = [
            {
                xtype: 'form',
                itemId: 'devicecommunicationprotocolform',
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
                                itemId: 'deviceCommunicationProtocolId',
                                readOnly: true,
                                autoWidth: true,
                                size: 10,
                                hidden: true

                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: 'Name',
                                autoWidth: true,
                                size: 50
                            },
                            {
                                xtype: 'textfield',
                                name: 'deviceProtocolVersion',
                                fieldLabel: 'Version',
                                autoWidth: true,
                                readOnly: true,
                                size: 50
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'form',
                itemId: 'licensedprotocolform',
                shrinkWrap: 1,
                padding: 10,
                border: 0,
                defaults: {
                    labelWidth: 200
                },
                items: [

                    {
                        xtype: 'fieldset',
                        title: 'Licensed Protocol Info',
                        defaults: {
                            labelWidth: 200
                        },
                        collapsible: true,
                        layout: 'anchor',
                        items: [
                            {
                                xtype: 'textfield',
                                name: 'protocolJavaClassName',
                                fieldLabel: 'Java Class Name',
                                itemId: 'protocolJavaClassName',
                                readOnly: true,
                                autoWidth: true,
                                size: 75
                            },
                            {"xtype": 'setupProtocolFamilies'}
                        ]
                    }
                ]
            },
            {
                xtype: 'propertyEdit'
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
})
;