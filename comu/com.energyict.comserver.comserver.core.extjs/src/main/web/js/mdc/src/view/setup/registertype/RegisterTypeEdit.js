Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',

    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ReadingTypeCombo'
    ],

    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    edit: false,

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.edit = edit;
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'form',
                border: false,
                itemId: 'registerTypeEditForm',
                width: '100%',
                ui: 'large',
                defaults: {
                    labelWidth: 250,
                    width: 700
                },
                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'registerTypeEditCreateInformation',
                        margin: '0 0 20 0',
                        hidden: true
                    },
                    {
                        itemId: 'readingTypeCombo',
                        xtype: 'reading-type-combo',
                        name: 'readingType',
                        fieldLabel: Uni.I18n.translate('general.readingtype', 'MDC', 'Reading type'),
                        displayField: 'fullAliasName',
                        valueField: 'name',
                        forceSelection: true,
                        store: 'AvailableReadingTypesForRegisterType',

                        listConfig: {
                            cls: 'isu-combo-color-list',
                            emptyText: Uni.I18n.translate('general.readingtype.startTypingToSelect', 'MDC', 'Start typing to select a reading type...')
                        },

                        listeners: {
                            blur: function (combo) {
                                if (combo.store.loading) {
                                    Ext.Ajax.suspendEvent('requestexception');
                                    Ext.Ajax.abortAll();
                                    combo.reset();
                                    Ext.Ajax.resumeEvent('requestexception');
                                }
                            }
                        },

                        queryMode: 'remote',
                        queryParam: 'like',
                        queryDelay: 500,
                        queryCaching: false,
                        minChars: 1,
                        required: true,
                        editable: true,
                        typeAhead: true,
                        emptyText: Uni.I18n.translate('general.readingtype.selectreadingtype', 'MDC', 'Start typing to select a reading type...')
                    },
                    {
                        xtype: 'obis-field',
                        itemId: 'editObisCodeField',
                        name: 'obisCode'
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: '&nbsp;',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
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
                                xtype: 'button',
                                ui: 'link',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                itemId: 'cancelLink',
                                href: '#/administration/registertypes/'
                            }
                        ]
                    }
                ]
            }
        ]
        ;
        this.callParent(arguments);

        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editRegisterType';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createRegisterType';
        }
        this.down('#cancelLink').href = this.returnLink;
    }

})
;
