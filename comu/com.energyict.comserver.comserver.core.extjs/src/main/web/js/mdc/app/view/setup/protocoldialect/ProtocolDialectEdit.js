Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectEdit',
    itemId: 'protocolDialectEdit',
    cls: 'content-container',
    edit: false,
    isEdit: function () {
        return this.edit
    },
    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#addEditButton').action = 'editProtocolDialect';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addProtocolDialect';
        }
        this.down('#cancelLink').autoEl.href = returnLink;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                width: '100%',
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
                        itemId: 'protocolDialectEditAddTitle',
                        margins: '10 10 10 10'
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'protocolDialectEditForm',
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
                                        disabled: true,
                                        readOnly: true,
                                        fieldLabel: Uni.I18n.translate('protocolDialect.name', 'MDC', 'Name'),
                                        itemId: 'editProtocolDialectNameField',
                                        width: 650

                                    }
                                ]
                            },
                            {
                                xtype: 'propertyEdit',
                                propertiesTitle: Uni.I18n.translate('protocolDialect.protocolDialectProperties', 'MDC', 'Properties'),
                                width: '100%',
                                padding: '10 10 0 10'
                            }
                        ]},
                    {
                        xtype: 'container',
                        margins: '10 10 10 10',
                        width: '100%',
                        items: [

                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'protocolDialectEditButtonsForm',

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
                                                itemId: 'addEditButton'
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
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.edit', 'MDC', 'Edit'));
            this.down('#addEditButton').action = 'editProtocolDialect';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createProtocolDialect';
        }
        this.down('#cancelLink').autoEl.href = this.returnLink;

    }


});



