Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.protocolDialectEdit',
    itemId: 'protocolDialectEdit',
    edit: false,

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
            this.down('#addEditButton').action = 'editProtocolDialect';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addProtocolDialect';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox'
                    //align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'protocolDialectEditAddTitle'
                    },
                    {
                        //   xtype: 'container',
                        //   items: [
                        //      {
                        xtype: 'form',
                        border: false,
                        itemId: 'protocolDialectEditForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            /* {
                             xtype: 'displayfield',
                             name: 'name',
                             fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                             itemId: 'editProtocolDialectNameField',
                             width: 650
                             }*/
                        ]
                    },
                    {
                        xtype: 'property-form',
                        width: '100%'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'protocolDialectEditButtonsForm',
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
                                        href: '#/administration/devicetypes/'

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
            this.down('#addEditButton').action = 'editProtocolDialect';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createProtocolDialect';
        }
        this.down('#cancelLink').href = this.returnLink;

    }

})
;



