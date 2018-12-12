/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.AddReadingQualityWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.add-reading-quality-window',
    itemId: 'uni-add-reading-quality-window',
    closable: false,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    comboBoxValueForAll: -1,
    width: 450,
    defaultFocus: 'uni-reading-quality-input-field',
    requires: [
        'Uni.util.FormErrorMessage'
    ],
    readingQualities: null,
    items: {
        xtype: 'form',
        margins: '5 5 5 5',
        border: false,
        itemId: 'readingQualityForm',
        items: [
            {
                xtype: 'uni-form-error-message',
                name: 'form-errors',
                itemId: 'form-errors',
                margin: '10 0 10 0',
                hidden: true
            },
            {
                xtype: 'radiogroup',
                itemId: 'uni-reading-quality-input-method',
                required: true,
                width: 400,
                columns: 1,
                vertical: true,
                items: [
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'radio',
                                itemId: 'uni-reading-quality-input-radio',
                                boxLabel: '<span style="display:inline-block; float: left; margin-right:7px;" >' + Uni.I18n.translate('general.specifyReadingQuality', 'UNI', 'Specify reading quality') + '</span>'
                                + '<span class="icon-info" style="cursor:default; display:inline-block; color:#A9A9A9; font-size:16px;" data-qtip="'
                                + Uni.I18n.translate('general.specifyReadingQuality.tooltip', 'UNI', "The wildcard '*' can be used in the 1st and 3rd part of the reading quality and will match all possible values.")
                                + '"></span>',
                                name: 'manualInput',
                                inputValue: true,
                                margin: '10 0 0 0',
                                checked: true
                            },
                            {
                                xtype: 'form',
                                margins: '5 0 0 0',
                                border: false,
                                itemId: 'uni-reading-quality-form',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        itemId: 'uni-reading-quality-input-field',
                                        emptyText: 'x.x.x',
                                        width: 380,
                                        fieldLabel: Uni.I18n.translate('general.readingQuality', 'UNI', 'Reading quality'),
                                        required: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'vbox',
                        items: [
                            {
                                xtype: 'radio',
                                itemId: 'uni-reading-quality-parts-input-radio',
                                boxLabel: Uni.I18n.translate('general.specifyReadingQualityParts', 'UNI', 'Specify reading quality parts'),
                                name: 'manualInput',
                                inputValue: false
                            },
                            {
                                xtype: 'form',
                                border: false,
                                itemId: 'specifyEventPartsForm',
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 150
                                },
                                items: [
                                    {
                                        xtype: 'combobox',
                                        itemId: 'uni-system-combo',
                                        disabled: true,
                                        width: 380,
                                        fieldLabel: Uni.I18n.translate('general.readingQuality.field1.name', 'UNI', 'System'),
                                        emptyText: Uni.I18n.translate('general.readingQuality.field1.emptyText', 'UNI', 'Select a system...'),
                                        required: true,
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'name',
                                        valueField: 'id'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'uni-category-combo',
                                        disabled: true,
                                        width: 380,
                                        fieldLabel: Uni.I18n.translate('general.readingQuality.field2.name', 'UNI', 'Category'),
                                        emptyText: Uni.I18n.translate('general.readingQuality.field2.emptyText', 'UNI', 'Select a category...'),
                                        required: true,
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'name',
                                        valueField: 'id'
                                    },
                                    {
                                        xtype: 'combobox',
                                        itemId: 'uni-index-combo',
                                        width: 380,
                                        disabled: true,
                                        fieldLabel: Uni.I18n.translate('general.readingQuality.field3.name', 'UNI', 'Index'),
                                        emptyText: Uni.I18n.translate('general.readingQuality.field3.emptyText', 'UNI', 'Select an index...'),
                                        required: true,
                                        queryMode: 'local',
                                        editable: false,
                                        displayField: 'name',
                                        valueField: 'id'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.bbar = [
            {
                xtype: 'container',
                width: 173
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                ui: 'action',
                itemId: 'uni-reading-quality-add-button',
                listeners: {
                    click: {
                        fn: function () {
                            var inputRadioBtn = this.up('#uni-add-reading-quality-window').down('#uni-reading-quality-input-radio'),
                                enteredValue = inputRadioBtn.getValue()
                                    ? this.up('#uni-add-reading-quality-window').down('#uni-reading-quality-input-field').getValue()
                                    : this.up('#uni-add-reading-quality-window').down('#uni-system-combo').getValue()
                                + '.' +
                                this.up('#uni-add-reading-quality-window').down('#uni-category-combo').getValue()
                                + '.' +
                                this.up('#uni-add-reading-quality-window').down('#uni-index-combo').getValue(),
                                validationResult = me.readingQualities.isCimCodeInvalid(enteredValue);

                            if (validationResult === 0) {
                                me.readingQualities.addReadingQuality(enteredValue);
                                this.up('#uni-add-reading-quality-window').destroy();
                            } else {
                                me.showErrorMessage(enteredValue, validationResult);
                            }
                        }
                    }
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
                action: 'cancelAddReadingQuality',
                ui: 'link',
                listeners: {
                    click: {
                        fn: function () {
                            this.up('#uni-add-reading-quality-window').destroy();
                        }
                    }
                }
            }
        ];

        me.on({
            afterrender: {
                fn: me.onAfterRender,
                scope: me,
                single: true
            }
        });

        this.callParent(arguments);
    },

    onAfterRender: function () {
        var me = this,
            radioButton = me.down('#uni-reading-quality-input-radio'),
            systemCombo = me.down('#uni-system-combo'),
            categoryCombo = me.down('#uni-category-combo'),
            indexCombo = me.down('#uni-index-combo');

        systemCombo.bindStore(me.readingQualities.getSystemStore(), true);
        categoryCombo.bindStore(me.readingQualities.getCategoryStore(), true);
        indexCombo.setDisabled(true);

        radioButton.on("change", me.onRadioBtnChange, me);
        categoryCombo.on("change", me.onCategoryComboChanged, me);
    },

    onRadioBtnChange: function (field, newValue, oldValue) {
        var me = this,
            systemCombo = me.down('#uni-system-combo'),
            categoryCombo = me.down('#uni-category-combo'),
            indexCombo = me.down('#uni-index-combo');
        fieldToFocus = newValue ? me.down('#uni-reading-quality-input-field') : me.down('#uni-system-combo');

        me.down('#uni-reading-quality-form').setDisabled(!newValue);
        systemCombo.setDisabled(newValue);
        categoryCombo.setDisabled(newValue);
        indexCombo.setDisabled(newValue ? newValue : Ext.isEmpty(categoryCombo.getValue()));
        fieldToFocus.focus(false, 200);
        me.hideErrorMessage();
    },

    onCategoryComboChanged: function (combo, newValue, oldValue) {
        var me = this,
            indexCombo = me.down('#uni-index-combo');

        indexCombo.bindStore(me.readingQualities.getIndexStore(newValue), true);
        indexCombo.setValue(null);
        indexCombo.setDisabled(false);
    },

    showErrorMessage: function (enteredValue, validationResult) {
        this.down('#form-errors').show();
        if (this.down('#uni-reading-quality-input-radio').getValue()) {
            if (Ext.isEmpty(enteredValue)) {
                this.down('#uni-reading-quality-input-field').markInvalid(Uni.I18n.translate('general.requiredField', 'UNI', 'This field is required'));
            } else {
                this.down('#uni-reading-quality-input-field').markInvalid(Uni.I18n.translate('general.readingQuality.invalid', 'UNI', 'Reading quality is invalid'));
            }
        } else {
            if (validationResult & 2) {
                this.down('#uni-system-combo').markInvalid(Uni.I18n.translate('general.requiredField', 'UNI', 'This field is required'));
            }
            if (validationResult & 4) {
                this.down('#uni-category-combo').markInvalid(Uni.I18n.translate('general.requiredField', 'UNI', 'This field is required'));
            }
            if (validationResult & 8) {
                this.down('#uni-index-combo').markInvalid(Uni.I18n.translate('general.requiredField', 'UNI', 'This field is required'));
            }
        }
    },

    hideErrorMessage: function () {
        this.down('#form-errors').hide();
        this.down('#uni-reading-quality-input-field').clearInvalid();
        this.down('#uni-system-combo').clearInvalid();
        this.down('#uni-category-combo').clearInvalid();
        this.down('#uni-index-combo').clearInvalid();
    }

});