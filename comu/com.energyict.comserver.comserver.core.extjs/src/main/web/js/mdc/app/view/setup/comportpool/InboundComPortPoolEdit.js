Ext.define('Mdc.view.setup.comportpool.InboundComPortPoolEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.inboundComPortPoolEdit',
    autoScroll: true,

    requires: [
        'Mdc.view.setup.comport.PoolInboundComPorts',
        'Mdc.store.DeviceDiscoveryProtocols'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
        var comporttypes = Ext.create('Mdc.store.ComPortTypes');
        var discoveryProtocols =  Ext.create('Mdc.store.DeviceDiscoveryProtocols');
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
                                name: 'name',
                                fieldLabel: 'Name'
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: 'Description'
                            },
                            {
                                xtype: 'combobox',
                                name: 'type',
                                fieldLabel: 'Communication port type',
                                store: comporttypes,
                                queryMode: 'local',
                                displayField: 'comPortType',
                                valueField: 'comPortType'
                            },
                            {
                                xtype: 'checkbox',
                                name: 'active',
                                inputValue: true,
                                uncheckedValue: 'false',
                                fieldLabel: 'active'
                            },
                            {
                                xtype: 'combobox',
                                name: 'discoveryProtocolPluggableClassId',
                                fieldLabel: 'Discovery protocol',
                                store: discoveryProtocols,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            }
                        ]},
                    {
                        "xtype": 'poolInboundComPorts'
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


