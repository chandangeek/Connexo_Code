Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceProtocolDialectEdit',
    itemId: 'deviceProtocolDialectEdit',
    edit: false,
    mRID: null,

    required: [
        'Uni.property.form.Property'
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceProtocolDialect';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addDeviceProtocolDialect';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'protocolLink'
                    }
                ]
            }
        ];
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'deviceProtocolDialectEditAddTitle',
                layout: {
                    type: 'vbox'
                },

                items: [
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceProtocolDialectEditForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                        ]
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'editProtocolDialectsDetailsTitle',
                        hidden: true,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: '<h3>' + Uni.I18n.translate('protocolDialect.protocolDialectDetails', 'MDC', 'Protocol dialect details') + '</h3>',
                                text: ''
                            }
                        ]
                    },
                    {
                        xtype: 'property-form',
                        width: '100%'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceProtocolDialectEditButtonsForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'createAction',
                                        itemId: 'addEditButton'
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                                        icon: '../sky/build/resources/images/form/restore.png',
                                        itemId: 'restoreAllButton',
                                        action: 'restoreAll'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/devices/' + encodeURIComponent(this.mRID) + '/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        ;
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceProtocolDialect';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createDeviceProtocolDialect';
        }
        this.down('#cancelLink').href = this.returnLink;

    }

})
;



