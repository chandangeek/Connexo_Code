Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationEdit',
    itemId: 'deviceConfigurationEdit',
    autoScroll: true,
    cls: 'content-container',
    edit: false,
    isEdit: function(){
        return this.edit
    },
    setEdit: function(edit,returnLink){
        if(edit){
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceConfiguration';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceConfiguration';
        }
        this.down('#cancelLink').autoEl.href=returnLink;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceConfigurationEditCreateTitle',
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
                                        itemId: 'deviceConfigurationEditForm',
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
                                                xtype: 'textfield',
                                                name: 'name',
                                                validator:function(text){
                                                    if(Ext.util.Format.trim(text).length==0)
                                                        return Uni.I18n.translate('deviceconfiguration.emptyName', 'MDC', 'The name of a device configuration can not be empty.')
                                                    else
                                                        return true;
                                                },
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceConfigurationNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'textareafield',
                                                name: 'description',
                                                msgTarget: 'under',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.description', 'MDC', 'Description'),
                                                itemId: 'editDeviceConfigurationDescriptionField'
                                            },
                                            {
                                                xtype: 'checkbox',
                                                inputValue: true,
                                                uncheckedValue: 'false',
                                                name: 'canBeGateway',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isGateway', 'MDC', 'Can act as gateway'),
                                                itemId: 'gatewayCheckbox',
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: ' ',
                                                layout: {
                                                    type: 'vbox'
                                                },
                                                hidden: true,
                                                itemId: 'gatewayMessage',
                                                items: [
                                                    {
                                                        xtype: 'component',
                                                        cls: 'x-form-display-field',
                                                        html: '<i>'+Uni.I18n.translate('deviceconfiguration.gatewayMessage', 'MDC', 'The device cannot act as a gateway')+'</i>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'checkbox',
                                                inputValue: true,
                                                uncheckedValue: 'false',
                                                name: 'isDirectlyAddressable',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                                itemId: 'addressableCheckbox',
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: ' ',
                                                layout: {
                                                    type: 'vbox'
                                                },
                                                hidden: true,
                                                itemId: 'addressableMessage',
                                                items: [
                                                    {
                                                        xtype: 'component',
                                                        cls: 'x-form-display-field',
                                                        html: '<i>'+Uni.I18n.translate('deviceconfiguration.directlyAddressableMessage', 'MDC', 'The device cannot be directly addressed')+'</i>'
                                                    }
                                                ]
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
            this.down('#createEditButton').action = 'editDeviceConfiguration';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceConfiguration';
        }
        this.down('#cancelLink').autoEl.href=this.returnLink;

    }


});

