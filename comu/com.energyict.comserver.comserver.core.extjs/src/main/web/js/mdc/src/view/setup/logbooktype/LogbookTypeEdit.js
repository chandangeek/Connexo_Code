Ext.define('Mdc.view.setup.logbooktype.LogbookTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.logbookTypeEdit',
    itemId: 'logbookTypeEdit',
    requires: [
        'Uni.form.field.Obis',
        'Uni.util.FormErrorMessage'
    ],
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'logbookTypeEditCreateTitle',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'logbookTypeEditCreateInformation',
                        margin: '0 0 20 0',
                        hidden: true
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        items: [
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'logbookTypeEditForm',
                                layout: {
                                    type: 'vbox'
                                },
                                defaults: {
                                    labelWidth: 250,
                                    width: 650
                                },
                                items: [
                                    {
                                        xtype: 'uni-form-error-message',
                                        name: 'errors',
                                        hidden: true,
                                        margin: '0 0 32 0'
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'name',
                                        msgTarget: 'under',
                                        required: true,
                                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                        itemId: 'editLogbookTypeNameField',
                                        maxLength: 80,
                                        enforceMaxLength: true,
                                        width: 600
                                    },
                                    {
                                        xtype: 'obis-field',
                                        itemId: 'editObisCodeField',
                                        name: 'obisCode'
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
                                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
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
                                                href: '#/administration/logbooktypes/'
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
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editLogbookType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createLogbookType';
        }
        this.down('#cancelLink').href = this.returnLink;
    }
});


