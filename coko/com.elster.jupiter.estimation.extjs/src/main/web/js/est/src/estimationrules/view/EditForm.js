Ext.define('Est.estimationrules.view.EditForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.grid.column.ReadingType'
    ],
    alias: 'widget.estimation-rule-edit-form',
    ui: 'large',
    edit: false,
    returnLink: undefined,
    defaults: {
        labelWidth: 260,
        width: 600
    },
    initComponent: function () {
        var me = this;

        me.title = me.edit ? '&nbsp;' : Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule');

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 10 0',
                hidden: true
            },
            {
                xtype: 'textfield',
                itemId: 'name-field',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.estimationRule', 'EST', 'Estimation rule'),
                required: true,
                emptyText: Uni.I18n.translate('general.placeholder.enterName', 'EST', 'Enter a name...')
            },
            {
                xtype: 'combobox',
                itemId: 'estimator-field',
                name: 'implementation',
                fieldLabel: Uni.I18n.translate('estimationrules.estimator', 'EST', 'Estimator'),
                required: true,
                editable: 'false',
                store: 'Est.estimationrules.store.Estimators',
                valueField: 'implementation',
                displayField: 'displayName',
                queryMode: 'local',
                forceSelection: true,
                emptyText: Uni.I18n.translate('general.placeholder.selectEstimationRule', 'EST', 'Select a estimation rule...'),
                disabled: me.edit,
                listeners: {
                    change: {
                        fn: Ext.bind(me.onImplementationChange, me)
                    }
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'reading-types-field-container',
                fieldLabel: Uni.I18n.translate('general.readingTypes', 'EST', 'Reading types'),
                required: true,
                layout: 'hbox',
                width: 1100,
                items: [
                    {
                        xtype: 'gridpanel',
                        itemId: 'reading-types-grid',
                        store: 'ext-empty-store',
                        hideHeaders: true,
                        padding: 0,
                        scroll: 'vertical',
                        columns: [
                            {
                                xtype: 'reading-type-column',
                                dataIndex: 'readingType',
                                flex: 1
                            },
                            {
                                xtype: 'actioncolumn',
                                align: 'right',
                                items: [
                                    {
                                        iconCls: 'uni-icon-delete',
                                        handler: function (grid, rowIndex) {
                                            grid.getStore().removeAt(rowIndex);
                                        }
                                    }
                                ]
                            }
                        ],
                        height: 220,
                        width: 670,
                        dockedItems: [
                            {
                                xtype: 'component',
                                dock: 'bottom',
                                cls: 'x-form-invalid-under',
                                itemId: 'reading-types-grid-error',
                                height: 52,
                                hidden: true
                            }
                        ]
                    },
                    {
                        xtype: 'button',
                        itemId: 'add-reading-types-button',
                        text: Uni.I18n.translate('general.addReadingTypes', 'EST', 'Add reading types'),
                        action: 'addReadingTypes',
                        margin: '0 0 0 10'
                    }
                ]
            },
            {
                xtype: 'property-form',
                itemId: 'property-form',
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    hasNotValueSameAsDefaultMessage: true
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'edit-rule-button',
                        text: me.edit ? Uni.I18n.translate('general.save', 'EST', 'Save') : Uni.I18n.translate('general.add', 'EST', 'Add'),
                        ui: 'action',
                        action: 'editRule'
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancel-edit-rule-button',
                        text: Uni.I18n.translate('general.cancel', 'EST', 'Cancel'),
                        ui: 'link',
                        action: 'cancelEditRule',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },
    suspendImplementationChange: false,
    onImplementationChange: function (implementationCombo, newValue) {
        var me = this,
            estimator = implementationCombo.getStore().getById(newValue);

        if (estimator && !me.suspendImplementationChange) {
            me.down('property-form').loadRecord(estimator);
            me.updateLayout();
        }
    },
    loadRecord: function (record) {
        var me = this;

        me.suspendImplementationChange = true;
        me.callParent(arguments);
        me.down('property-form').loadRecord(record);
        me.suspendImplementationChange = false;

        Ext.suspendLayouts();
        if (record.getId()) {
            me.setTitle(Uni.I18n.translate('general.edit', 'EST', 'Edit') + ' \'' + Ext.String.htmlEncode(record.get('name')) + '\'');
        }
        if (record.readingTypes().count()) {
            me.down('#reading-types-grid').reconfigure(record.readingTypes());
        }
        me.updateLayout();
        Ext.resumeLayouts(true);
    },
    updateRecord: function () {
        var me = this,
            propertyForm = me.down('property-form');

        me.callParent(arguments);

        if (propertyForm.getRecord()) {
            propertyForm.updateRecord();
            me.getRecord().propertiesStore = propertyForm.getRecord().properties();
        }
    },
    updateValid: function (errors) {
        var me = this,
            baseForm = me.getForm(),
            errorsPanel = me.down('uni-form-error-message'),
            readingTypesField = me.down('#reading-types-grid'),
            readingTypesFieldError = me.down('#reading-types-grid-error'),
            readingTypesError;

        Ext.suspendLayouts();
        if (errors && errors.length) {
            readingTypesError = Ext.Array.findBy(errors, function (item) {return item.id === 'readingTypesInRule'});
            errorsPanel.show();
            if (readingTypesError) {
                readingTypesField.addCls('error-border');
                readingTypesFieldError.update(readingTypesError.msg);
                readingTypesFieldError.show();
            }
            baseForm.markInvalid(errors);
            me.down('property-form').markInvalid(errors);
        } else {
            errorsPanel.hide();
            readingTypesField.removeCls('error-border');
            readingTypesFieldError.hide();
            baseForm.clearInvalid();
            me.down('property-form').clearInvalid(errors);
        }
        Ext.resumeLayouts(true);
    }
});