Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeEditForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.form.field.Obis'
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
            name: 'name',
            regex: /[a-zA-Z0-9]+/,
            allowBlank: false,
            required: true,
            fieldLabel: 'Name',
            msgTarget: 'under'
        },
        {
            xtype: 'combobox',
            allowBlank: false,
            store: 'Mdc.store.Intervals',
            fieldLabel: 'Interval',
            emptyText: '0 minutes',
            name: 'timeDuration',
            displayField: 'name',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            required: true,
            editable: false
        },
        {
            xtype: 'obis-field',
            fieldLabel: 'OBIS code',
            name: 'obisCode',
            msgTarget: 'under'
        },
        {
            xtype: 'fieldcontainer',
            fieldLabel: 'Measurement types',
            itemId: 'measurement-types-fieldcontainer',
            required: true,
            msgTarget: 'under',
            items: [
                {
                    xtype: 'gridpanel',
                    itemId: 'measurement-types-grid',
                    hideHeaders: true,
                    store: 'Mdc.store.SelectedMeasurementTypesForLoadProfileType',
                    padding: 0,
                    columns: [
                        {
                            text: 'Name',
                            dataIndex: 'name',
                            flex: 1
                        },
                        {
                            xtype: 'actioncolumn',
                            iconCls: 'icon-delete',
                            align: 'right'
                        }
                    ],
                    height: 220,
                    width: 700,
                    rbar: [
                        {
                            xtype: 'container',
                            items: [
                                {
                                    xtype: 'button',
                                    itemId: 'add-measurement-types-to-load-profile-type-button',
                                    text: Uni.I18n.translate('loadProfileTypes.addMeasurementTypes', 'MDC', 'Add measurement types'),
                                    ui: 'action',
                                    margin: '0 0 0 10'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'component',
                    itemId: 'measurement-types-errors',
                    cls: 'x-form-invalid-under',
                    hidden: true,
                    height: 36
                }
            ],
            markInvalid: function (msg) {
                var errorComponent = this.down('#measurement-types-errors');

                errorComponent.update(msg);
                errorComponent.show();
                this.down('#measurement-types-grid').setUI('wrong-data');
            },
            clearInvalid: function () {
                this.down('#measurement-types-errors').hide();
                this.down('#measurement-types-grid').setUI('default');
            }
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

    setEdit: function (edit, returnLink, addMeasurementTypesLink) {
        this.edit = edit;

        if (edit) {
            this.down('#save-load-profile-type-button').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
        } else {
            this.down('#save-load-profile-type-button').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
        }

        this.down('#cancel-edit-load-profile-type-button').setHref(returnLink);
        this.down('#cancel-edit-load-profile-type-button').on('click', function () {
            location.href = returnLink;
        });
        this.down('#add-measurement-types-to-load-profile-type-button').setHref(addMeasurementTypesLink);
        this.down('#add-measurement-types-to-load-profile-type-button').on('click', function () {
            location.href = addMeasurementTypesLink;
        });
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
            } else if (key === 'measurementTypes' && Ext.isArray(value)) {
                me.down('#measurement-types-grid').getStore().loadData(value, true);
            } else if (key === 'measurementTypes') {
                me.down('#measurement-types-grid').getStore().removeAll();
            }
        });
    },

    updateRecord: function(record) {
        var me = this,
            basicForm = me.getForm(),
            measurementTypes = [],
            obj;

        record = record || basicForm._record;
        if (!record) {
            //<debug>
            Ext.Error.raise("A record is required.");
            //</debug>
            return basicForm;
        }

        obj = me.getValues();

        me.down('#measurement-types-grid').getStore().each(function (model) {
            measurementTypes.push(model.getData());
        });

        obj.measurementTypes = measurementTypes;

        record.beginEdit();
        Ext.iterate(obj, function (key, value) {
            record.set(key, value);
        });
        record.set('timeDuration', {id: obj.timeDuration});
        record.endEdit();

        return basicForm;
    }
});

