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
    edit: false,
    isEdit: function(){
        return this.edit
    },
    setEdit: function(edit,returnLink){
        if(edit){
            this.edit = edit;
            this.down('#createEditButton').setText(I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').autoEl.href=returnLink;
    },

    initComponent: function () {
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
                        xtype: 'breadcrumbTrail',
                        region: 'north',
                        padding: 6
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
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
                                        defaults:{
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'combobox',
                                                name: 'communicationProtocolName',
                                                fieldLabel: I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device Communication protocol'),
                                                itemId: 'communicationProtocolComboBox',
                                                store: this.deviceCommunicationProtocols,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'name',
                                                required: true
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                vtype: 'nonemptystring',
                                                required: true,
                                                fieldLabel: I18n.translate('devicetype.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceTypeNameField'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                fieldLabel: '&nbsp',
                                                //width: 430,
                                                layout: {
                                                    type: 'hbox',
                                                    align: 'stretch'
                                                },
                                                items: [
                                                    {
                                                        text: I18n.translate('general.create', 'MDC', 'Create'),
                                                        xtype: 'button',
                                                        action: 'createAction',
                                                        itemId: 'createEditButton'
                                                    },
                                                    {
                                                        xtype: 'component',
                                                        padding: '3 0 0 10',
                                                        itemId: 'cancelLink',
                                                        autoEl: {
                                                            tag: 'a',
                                                            href: '#setup/devicetypes/',
                                                            html: I18n.translate('general.cancel', 'MDC', 'Cancel')
                                                        }
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }


                ]
            }
        ];
        this.callParent(arguments);
        if(this.isEdit()){
            this.down('#createEditButton').setText(I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.down('#createEditButton').setText(I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').autoEl.href=this.returnLink;

    }


});
