Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Ext.container.Container',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    autoScroll: true,
    requires: [
        'Mdc.store.DeviceCommunicationProtocols'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-wrapper',
    setEdit: function(edit,returnLink){
        if(edit){
            this.down('#createEditButton').setText('Edit');
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.down('#createEditButton').setText('Create');
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').autoEl.href=returnLink;
    },

    initComponent: function () {
        var deviceCommunicationProtocols = Ext.create('Mdc.store.DeviceCommunicationProtocols');
        this.items = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [

                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceTypeEditForm',
                        padding: '10 10 0 10',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
//                    tbar: [
//                        {
//                            xtype: 'component',
//                            html: '<h4>Overview</h4>',
//                            itemId: 'deviceTypePreviewTitle'
//                        }
//                    ],

                        items: [
                            {
                                xtype: 'combobox',
                                name: 'communicationProtocolName',
                                fieldLabel: 'Device Communication protocol:',
                                store: deviceCommunicationProtocols,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'name',
                                labelWidth:	150
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                fieldLabel: 'Name:',
                                labelAlign: 'right',
                                labelWidth:	150
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                labelAlign: 'right',
                                labelWidth:	150,
                                //width: 430,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: 'Create',
                                        xtype: 'button',
                                        action: 'createAction',
                                        itemId: 'createEditButton'
                                    },
                                    {
                                        xtype: 'component',
                                        padding: '2 0 0 10',
                                        itemId: 'cancelLink',
                                        autoEl: {
                                            tag: 'a',
                                            href: '#setup/devicetypes/',
                                            html: 'Cancel'
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }


});
