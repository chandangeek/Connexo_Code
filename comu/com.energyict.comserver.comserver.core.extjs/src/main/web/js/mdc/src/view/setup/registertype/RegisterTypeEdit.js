/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.registertype.RegisterTypeEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerTypeEdit',
    itemId: 'registerTypeEdit',

    requires: [
        'Uni.form.field.Obis',
        'Uni.form.field.ReadingTypeDisplay',
        'Uni.form.field.ReadingTypeCombo',
        'Uni.util.FormErrorMessage'
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
                        xtype: 'uni-form-error-message',
                        itemId: 'registerTypeEditFormErrors',
                        name: 'errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'editObisCodeFieldWithTooltip',
                        layout: 'hbox',
                        width: 750,
                        required: true,
                        items: [
                            {
                                xtype: 'obis-field',
                                itemId: 'editObisCodeField',
                                labelWidth: 250,
                                width: 700,
                                afterSubTpl: null,
                                name: 'obisCode'
                            },
                            {
                                xtype: 'button',
                                tooltip: Uni.I18n.translate('general.obisformat.tooltip', 'MDC', 'Provide the values for the 6 attributes of the OBIS code, separated by a "."'),
                                text: '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;"></span>',
                                disabled: true, // to avoid a hand cursor
                                ui: 'blank',
                                itemId: 'obisFormatHelp',
                                shadow: false,
                                margin: '6 0 0 6',
                                width: 16
                            }
                        ]
                    },
                    {
                        xtype: 'component',
                        html:  Uni.I18n.translate('general.readingtype.readingTypeToObisMappingMessage', 'MDC', 'The selected reading type could not be mapped to an OBIS code'),
                        itemId: 'readingTypeToObisMappingMessage',
                        style: {
                            'font': 'italic 13px/17px Lato',
                            'color': '#686868',
                            'margin-top': '6px',
                            'margin-bottom': '6px',
                            'margin-left': '250px'
                        },
                        hidden: true
                    },
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        width: 1000,
                        items: [
                            {
                                itemId: 'readingTypeCombo',
                                xtype: 'reading-type-combo',
                                name: 'readingType',
                                fieldLabel: Uni.I18n.translate('general.readingtype', 'MDC', 'Reading type'),
                                allowBlank: true,
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
                                beforeBlur: function() {
                                    if (this.getRawValue().length === 0) {
                                        this.lastSelection = [];
                                    }
                                    this.self.superclass['beforeBlur'].call(this);
                                },
                                queryMode: 'remote',
                                queryParam: 'like',
                                queryDelay: 500,
                                queryCaching: false,
                                minChars: 1,
                                required: true,
                                editable: true,
                                typeAhead: true,
                                labelWidth: 250,
                                width: 700,
                                emptyText: Uni.I18n.translate('general.readingtype.selectreadingtype', 'MDC', 'Start typing to select a reading type...')
                            },
                            {
                                text: Uni.I18n.translate('general.readingtype.addReadingType', 'MDC', 'Add reading type'),
                                xtype: 'button',
                                margin: '0 0 0 10',
                                itemId: 'addReadingTypeButton',
                                hidden: !(Uni.Auth.hasPrivilegeInApp('privilege.administer.readingType','SYS'))
                            }
                        ]
                    },

                    {
                        xtype: 'component',
                        itemId: 'obisCodeToReadingTypeMessage',
                        style: {
                            'font': 'italic 13px/17px Lato',
                            'color': '#686868',
                            'margin-top': '6px',
                            'margin-left': '250px'
                        },
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: '&nbsp;',
                        margin:'20 0 0 0',
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
