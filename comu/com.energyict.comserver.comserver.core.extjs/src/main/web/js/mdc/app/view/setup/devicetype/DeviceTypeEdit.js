Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    requires: [
        'Mdc.store.DeviceCommunicationProtocols'
    ],

    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'form',
                width: '100%',
                itemId: 'deviceTypeEditForm',
                ui: 'large',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'combobox',
                        name: 'communicationProtocolName',
                        fieldLabel: Uni.I18n.translate('devicetype.communicationProtocol', 'MDC', 'Communication protocol'),
                        itemId: 'communicationProtocolComboBox',
                        store: this.deviceCommunicationProtocols,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'name',
                        emptyText: Uni.I18n.translate('devicetype.selectProtocol', 'MDC', 'Select a communication protocol...'),
                        required: true,
                        forceSelection: true,
                        typeAhead: true,
                        msgTarget: 'under',
                        width: 600

                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        validator: function (text) {
                            if (Ext.util.Format.trim(text).length == 0)
                                return Uni.I18n.translate('devicetype.emptyName', 'MDC', 'The name of a device type can not be empty.')
                            else
                                return true;
                        },
                        msgTarget: 'under',
                        required: true,
                        fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
                        itemId: 'editDeviceTypeNameField',
                        maxLength: 80,
                        enforceMaxLength: true,
                        width: 600
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.create', 'MDC', 'Create'),
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
                                href: '#/administration/devicetypes/'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceType';
        }
        this.down('#cancelLink').href = this.returnLink;
    }

});
