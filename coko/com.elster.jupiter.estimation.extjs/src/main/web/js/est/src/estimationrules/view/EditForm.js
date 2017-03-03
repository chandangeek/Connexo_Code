/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrules.view.EditForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.grid.column.ReadingType',
        'Uni.grid.column.RemoveAction'
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

        me.title = me.edit ? ' ' : Uni.I18n.translate('estimationrules.addEstimationRule', 'EST', 'Add estimation rule');

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
                allowBlank: false,
                emptyText: Uni.I18n.translate('general.placeholder.enterName', 'EST', 'Enter a name...'),
                listeners: {
                    afterrender: function(field) {
                        field.focus(false, 200);
                    }
                }
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
                allowBlank: false,
                emptyText: Uni.I18n.translate('general.placeholder.selectEstimator', 'EST', 'Select an estimator...'),
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
                        xtype: 'component',
                        html: Uni.I18n.translate('general.noReadingTypesAvailable','EST','No reading types have been added'),
                        itemId: 'noReadingTypesForEstimationRuleLabel',
                        hidden: true,
                        style: {
                            'font': 'italic 13px/17px Lato',
                            'color': '#686868',
                            'margin-top': '6px',
                            'margin-right': '10px'
                        }
                    },


                    {
                        xtype: 'gridpanel',
                        hidden: true,
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
                                xtype: 'uni-actioncolumn-remove',
                                align: 'right',
                                handler: function (grid, rowIndex) {
                                    grid.getStore().removeAt(rowIndex);
                                    if (grid.getStore().count() === 0) {
                                        me.updateGrid();
                                    }
                                }
                            }
                        ],
                        height: 220,
                        width: 670
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
                xtype: 'label',
                cls: 'x-form-invalid-under',
                itemId: 'reading-types-grid-error',
                margin: '0 0 0 275',
                hidden: true
            },

            {
                xtype: 'property-form',
                itemId: 'property-form',
                margin: '20 0 0 0',
                width: '100%',
                defaults: {
                    labelWidth: me.defaults.labelWidth,
                    width: 325,
                    resetButtonHidden: true,
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
            me.setTitle(Uni.I18n.translate('general.editx', 'EST', "Edit '{0}'",[record.get('name')]))
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
    isValid: function() {
        var me = this,
            valid = !me.down('#noReadingTypesForEstimationRuleLabel').isVisible();
        if(!valid) {
            me.down('#reading-types-grid-error').update(Uni.I18n.translate('general.fieldRequired', 'EST', 'This field is required'));
            me.down('#reading-types-grid-error').show();
        }
        return this.callParent(arguments) && valid;
    },
    updateValid: function (errors) {
        var me = this,
            baseForm = me.getForm(),
            errorsPanel = me.down('uni-form-error-message'),
            readingTypesFieldError = me.down('#reading-types-grid-error'),
            readingTypesError;

        Ext.suspendLayouts();
        if (errors && errors.length) {
            readingTypesError = Ext.Array.findBy(errors, function (item) {return item.id === 'readingTypesInRule'});
            errorsPanel.show();
            if (readingTypesError) {
                readingTypesFieldError.update(readingTypesError.msg);
                readingTypesFieldError.show();
            }
            baseForm.markInvalid(errors);
            me.down('property-form').markInvalid(errors);
        } else {
            errorsPanel.hide();
            readingTypesFieldError.hide();
            baseForm.clearInvalid();
            me.down('property-form').clearInvalid(errors);
        }
        Ext.resumeLayouts(true);
    },

    updateGrid: function() {
        var me = this,
            grid = me.down('#reading-types-grid'),
            emptyLabel = me.down('#noReadingTypesForEstimationRuleLabel');
        if (grid.getStore().count() === 0) {
            emptyLabel.show();
            grid.hide();
        } else {
            emptyLabel.hide();
            grid.show();
        }
    }
});