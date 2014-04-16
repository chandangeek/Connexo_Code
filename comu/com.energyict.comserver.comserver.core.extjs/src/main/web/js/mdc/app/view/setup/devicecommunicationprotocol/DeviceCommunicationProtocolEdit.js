Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolEdit',
    itemId: 'deviceCommunicationProtocolEdit',
    overflowY: true,
    cls: 'content-container',
    edit: false,
    requires: [
        'Mdc.view.setup.property.Edit'
    ],
    isEdit: function () {
        return this.edit
    },
    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            console.log(this.down('#createEditButton'));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceCommunicationProtocol';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceCommunicationProtocol';
        }
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                overflowY: true,
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
                        itemId: 'deviceCommunicationProtocolEditCreateTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'deviceCommunicationProtocolEditForm',
                                padding: '10 10 0 10',
                                width: ' 100%',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [

                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.name', 'MDC', 'Name'),
                                        itemId: 'editName',
                                        disabled: true,
                                        readOnly: true,
                                        width: 650
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'deviceProtocolVersion',
                                        msgTarget: 'under',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.version', 'MDC', 'Version'),
                                        itemId: 'editDeviceProtocolVersion',
                                        disabled: true,
                                        readOnly: true,
                                        width: 650
                                    }
                                ]
                            },
                            {
                                xtype: 'propertyEdit',
                                width: '100%',
                                padding: '10 10 0 10'
                            }
                        ]},
                    {
                        xtype: 'container',
                        margins: '10 10 10 10',
                        width:'100%',
                        items: [

                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'deviceCommunicationProtocolEditButtonsForm',

                                width: '100%',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'fieldcontainer',
                                        fieldLabel: '&nbsp',
                                        layout: {
                                            type: 'hbox',
                                            align: 'stretch'
                                        },
                                        width: '100%',
                                        items: [
                                            {
                                                text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
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
                                                    href: '#setup/devicecommunicationprotocols/',
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
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            console.log(this.down("#createEditButton"));
            this.down('#createEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#createEditButton').action = 'editDeviceCommunicationProtocol';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.create', 'MDC', 'Create'));
            this.down('#createEditButton').action = 'createDeviceCommunicationProtocol';
        }

    }
});

