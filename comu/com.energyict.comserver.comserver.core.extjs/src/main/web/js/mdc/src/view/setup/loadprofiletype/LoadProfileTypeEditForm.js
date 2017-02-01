/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEditForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.Obis',
        'Uni.grid.column.ReadingType',
        'Uni.grid.column.RemoveAction'
    ],
    alias: 'widget.load-profile-type-edit-form',
    ui: 'large',
    width: '100%',
    edit: false,
    defaults: {
        labelWidth: 150,
        validateOnChange: false,
        validateOnBlur: false,
        width: 700
    },
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'uni-form-error-message',
            name: 'errors',
            hidden: true,
            margin: '0 0 32 0'
        },
        {
            xtype: 'textfield',
            itemId: 'txt-load-profile-type-name',
            name: 'name',
            regex: /[a-zA-Z0-9]+/,
            required: true,
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            msgTarget: 'under'
        },
        {
            xtype: 'combobox',
            allowBlank: false,
            itemId: 'timeDuration',
            fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
            emptyText: Uni.I18n.translate('loadProfileTypes.interval.epmtyText', 'MDC', '0 minutes'),
            name: 'timeDuration',
            displayField: 'name',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            required: true,
            editable: true
        },
        {
            xtype: 'obis-field',
            itemId: 'txt-load-profile-type-obis-code',
            fieldLabel: Uni.I18n.translate('loadProfileTypes.obisCode', 'MDC', 'OBIS code'),
            name: 'obisCode',
            msgTarget: 'under',
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
            itemId: 'register-types-fieldcontainer',
            required: true,
            layout: 'hbox',
            width: 1000,
            msgTarget: 'under',
            items: [
                {
                    xtype: 'component',
                    html: Uni.I18n.translate('general.noRegisterTypesAdded', 'MDC', 'No register types have been added'),
                    itemId: 'noRegisterTypesAddedMsg',
                    style: {
                        'font': 'italic 13px/17px Lato',
                        'color': '#686868',
                        'margin-top': '6px',
                        'margin-right': '10px'
                    },
                    hidden: true
                },
                {
                    xtype: 'panel',
                    items: [
                        {
                            xtype: 'gridpanel',
                            itemId: 'register-types-grid',
                            hideHeaders: true,
                            store: 'Mdc.store.SelectedRegisterTypesForLoadProfileType',
                            padding: 0,
                            overflowY: 'hidden',
                            width: 535,
                            maxHeight: 320,
                            columns: [
                                {
                                    xtype: 'reading-type-column',
                                    dataIndex: 'readingType',
                                    flex: 1
                                },
                                {
                                    xtype: 'uni-actioncolumn-remove',
                                    align: 'right',
                                    handler: function (grid, rowIndex) {
                                        grid.getStore().removeAt(rowIndex);
                                        this.up('load-profile-type-edit-form').showGridOrMessage();
                                    }
                                }
                            ],
                            listeners: {
                                afterrender: function () {
                                    this.view.el.dom.style.overflowX = 'hidden'
                                }
                            }
                        },
                        {
                            xtype: 'displayfield',
                            itemId: 'all-register-types',
                            width: 535,
                            value: Uni.I18n.translate('loadProfileTypes.allRegisterTypes', 'MDC', 'All register types'),
                            hidden: true
                        },
                        {
                            xtype: 'hiddenfield',
                            itemId: 'all-register-types-field',
                            name: 'allRegisterTypes',
                            value: false
                        }
                    ]
                },
                {
                    xtype: 'button',
                    itemId: 'add-register-types-to-load-profile-type-button',
                    text: Uni.I18n.translate('loadProfileTypes.addRegisterTypes', 'MDC', 'Add register types'),
                    margin: '0 0 0 10'
                }
            ],
            markInvalid: function (msg) {
                var errorComponent = this.up('load-profile-type-edit-form').down('#register-types-errors');
                errorComponent.update(msg);
                errorComponent.show();
            },
            clearInvalid: function () {
                this.up('load-profile-type-edit-form').down('#register-types-errors').hide();
            }
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            items: [
                {
                    xtype: 'component',
                    itemId: 'register-types-errors',
                    cls: 'x-form-invalid-under',
                    hidden: true,
                    height: 36
                }
            ]
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: '&nbsp;',
            items: [
                {
                    xtype: 'button',
                    itemId: 'save-load-profile-type-button',
                    text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                    ui: 'action'
                },
                {
                    xtype: 'button',
                    itemId: 'cancel-edit-load-profile-type-button',
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    ui: 'link'
                }
            ]
        }
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink, addRegisterTypesLink) {
        this.edit = edit;

        Ext.suspendLayouts();
        if (edit) {
            this.down('#save-load-profile-type-button').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
        } else {
            this.down('#save-load-profile-type-button').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
        }

        this.down('#cancel-edit-load-profile-type-button').setHref(returnLink);
        this.down('#cancel-edit-load-profile-type-button').on('click', function () {
            location.href = returnLink;
        });
        this.down('#add-register-types-to-load-profile-type-button').setHref(addRegisterTypesLink);
        this.down('#add-register-types-to-load-profile-type-button').on('click', function () {
            location.href = addRegisterTypesLink;
        });

        Ext.resumeLayouts(true);
    },

    loadRecord: function (record) {
        var me = this;

        me.getForm()._record = record;

        Ext.iterate(record.getData(), function (key, value) {
            var formField = me.down('[name=' + key + ']');

            if (formField) {
                if (Ext.isObject(value)) {
                    formField.setValue(value.id);
                } else {
                    formField.setValue(value);
                }
            } else if (key === 'registerTypes' && Ext.isArray(value)) {
                me.down('#register-types-grid').getStore().loadData(value, true);
            } else if (key === 'registerTypes') {
                me.down('#register-types-grid').getStore().removeAll();
            }
        });
    },

    updateRecord: function (record) {
        var me = this,
            basicForm = me.getForm(),
            registerTypes = [],
            obj;

        record = record || basicForm._record;
        if (!record) {
            //<debug>
            Ext.Error.raise("A record is required.");
            //</debug>
            return basicForm;
        }

        obj = me.getValues();

        me.down('#register-types-grid').getStore().each(function (model) {
            registerTypes.push(model.getData());
        });

        obj.registerTypes = registerTypes;

        record.beginEdit();
        Ext.iterate(obj, function (key, value) {
            record.set(key, value);
        });
        if (me.down('#timeDuration').getValue() === null) {
            record.set('timeDuration', null);
        } else {
            record.set('timeDuration', {id: obj.timeDuration});
        }

        record.endEdit();
        me.showGridOrMessage();

        return basicForm;
    },

    showGridOrMessage : function() {
        var me = this,
            registerTypesStore = me.down('#register-types-grid').getStore(),
            allRegisterTypesField = me.down('#all-register-types');

        if ( allRegisterTypesField.hidden ) {
            if (registerTypesStore.getCount() > 0) {
                this.down('#noRegisterTypesAddedMsg').hide();
                this.down('#register-types-grid').show();
            } else {
                this.down('#noRegisterTypesAddedMsg').show();
                this.down('#register-types-grid').hide();
            }
        } else {
            this.down('#noRegisterTypesAddedMsg').hide();
            this.down('#register-types-grid').hide();
        }
    }

});

