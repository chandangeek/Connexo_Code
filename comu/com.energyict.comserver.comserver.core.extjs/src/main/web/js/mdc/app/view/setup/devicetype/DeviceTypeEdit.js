Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    autoScroll: true,
    requires: [
        'Mdc.store.DeviceCommunicationProtocols'
    ],
//    controllers: [
//        'Mdc.controller.setup.DeviceTypes'
//    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    cls: 'content-container',
    edit: false,
    isEdit: function(){
        return this.edit
    },
    setEdit: function(edit,returnLink){
        if(edit){
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
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
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceTypeEditCreateTitle',
                        margins: '10 10 10 10'
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
                                        defaults:{
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'combobox',
                                                name: 'communicationProtocolName',
                                                fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Device Communication protocol'),
                                                itemId: 'communicationProtocolComboBox',
                                                store: this.deviceCommunicationProtocols,
                                                queryMode: 'local',
                                                displayField: 'name',
                                                valueField: 'name',
                                                emptyText: Uni.I18n.translate('devicetype.selectProtocol', 'MDC', 'Select a communication protocol...'),
                                                required: true,
                                                forceSelection: true,
                                                typeAhead: true
                                            },
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                validator:function(text){
                                                    if(Ext.util.Format.trim(text).length==0)
                                                        return Uni.I18n.translate('devicetype.emptyName', 'MDC', 'The name of a device type can not be empty.')
                                                    else
                                                        return true;
                                                },
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceTypeNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
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
                                                        text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
                                                        xtype: 'button',
                                                        action: 'createAction',
                                                        itemId: 'createEditButton'
//                                                        formBind: true
                                                    },
                                                    {
                                                        xtype: 'component',
                                                        padding: '3 0 0 10',
                                                        itemId: 'cancelLink',
                                                        autoEl: {
                                                            tag: 'a',
                                                            href: '#setup/devicetypes/',
                                                            html: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').autoEl.href=this.returnLink;

    }


});
