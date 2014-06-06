Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceCommunicationProtocolEdit',
    itemId: 'deviceCommunicationProtocolEdit',
    overflowY: true,
    edit: false,

    requires: [
        'Mdc.view.setup.property.Edit'
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceCommunicationProtocol';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceCommunicationProtocol';
        }
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'container',
                overflowY: true,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceCommunicationProtocolEditCreateTitle'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'deviceCommunicationProtocolEditForm',
                                width: ' 100%',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.name', 'MDC', 'Name'),
                                        itemId: 'editName'
                                    },
                                    {
                                        xtype: 'displayfield',
                                        name: 'deviceProtocolVersion',
                                        fieldLabel: Uni.I18n.translate('deviceCommunicationProtocol.version', 'MDC', 'Version'),
                                        itemId: 'editDeviceProtocolVersion'
                                    }
                                ]
                            },
                            {
                                xtype: 'propertyEdit',
                                width: '100%'
                            },
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                labelWidth: 250,
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                width: '100%',
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'createAction',
                                        itemId: 'createEditButton'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/devicecommunicationprotocols/'
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceCommunicationProtocol';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceCommunicationProtocol';
        }
    }
});

