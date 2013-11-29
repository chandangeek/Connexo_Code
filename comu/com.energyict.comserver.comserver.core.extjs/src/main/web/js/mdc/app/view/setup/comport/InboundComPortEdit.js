Ext.define('Mdc.view.setup.comport.InboundComPortEdit', {
    extend: 'Ext.panel.Panel',
    requires: ['Mdc.store.ComPortPools','Mdc.store.ComPortTypes'],
    alias: 'widget.inboundComPortEdit',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    border: 0,


    initComponent: function () {
        var comporttypes = Ext.create('Mdc.store.ComPortTypes');
        var comportpools = Ext.create('Mdc.store.ComPortPools');
        comportpools.filter('direction','inbound');
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
                                xtype: 'combobox',
                                name: 'comPortType',
                                fieldLabel: 'Communication port type',
                                store: comporttypes,
                                queryMode: 'local',
                                displayField: 'comPortType',
                                valueField: 'comPortType'
                            },
                            {
                                xtype: 'textfield',
                                name: 'description',
                                fieldLabel: 'description'
                            },
                            {
                                xtype: 'numberfield',
                                name: 'numberOfSimultaneousConnections',
                                fieldLabel: 'numberOfSimultaneousConnections'
                            },
                            {
                                xtype: 'checkbox',
                                inputValue: true,
                                uncheckedValue: 'false',
                                name: 'active',
                                fieldLabel: 'active'
                            },
                            {
                                xtype: 'textfield',
                                name: 'portNumber',
                                fieldLabel: 'portNumber'
                            },
                            {
                                xtype: 'combobox',
                                name: 'comPortPool_id',
                                fieldLabel: 'Communication port pool',
                                store: comportpools,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            }
                        ]}
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

