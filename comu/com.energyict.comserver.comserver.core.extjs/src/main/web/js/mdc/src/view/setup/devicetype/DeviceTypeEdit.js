Ext.define('Mdc.view.setup.devicetype.DeviceTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypeEdit',
    itemId: 'deviceTypeEdit',
    requires: [
        'Mdc.store.DeviceCommunicationProtocols'
    ],

    edit: false,
    cancelLink: null,

    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        var me = this;

        this.content = [
            {
                xtype: 'form',
                width: '100%',
                itemId: 'deviceTypeEditForm',
                title: '&nbsp;',
                ui: 'large',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-info-message',
                        text: 'This device type has one or more device configurations. Only fields name and device life cycle are editable',
                        itemId: 'info-panel',
                        hidden: true,
                        width: 600,
                        iconCmp: {
                            xtype: 'box',
                            height: 16,
                            width: 16,
                            cls: 'uni-icon-info-small'
                        }
                    },
                    {
                        xtype: 'combobox',
                        name: 'deviceProtocolPluggableClass',
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
                        fieldLabel: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                        required: true,
                        itemId: 'device-life-cycle-field',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'device-life-cycle-combo',
                                name: 'deviceLifeCycleId',
                                width: 335,
                                store: 'Mdc.store.DeviceLifeCycles',
                                editable: false,
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id'
                            },
                            {
                                xtype: 'displayfield',
                                itemId: 'no-device-life-cycles',
                                hidden: true,
                                value: '<div style="color: #FF0000">' + Uni.I18n.translate('general.noDeviceLifeCyclesMsg', 'MDC', 'No device life cycles defined') + '</div>',
                                width: 335
                            }
                        ]
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
                                text: me.edit ? Uni.I18n.translate('general.save', 'MDC', 'Save') : Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                xtype: 'button',
                                ui: 'action',
                                action: me.edit ? 'editDeviceType' : 'createDeviceType',
                                itemId: 'createEditButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }

});
