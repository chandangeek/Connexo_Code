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
        //'Uni.util.FormErrorMessage',
    ],
    readingQualities: null,
    items: {
        xtype: 'form',
        margins: '5 5 5 5',
        border: false,
        itemId: 'readingQualityForm',
        items: [
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
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                itemId: 'uni-reading-quality-container',
                                required: true,
                                msgTarget: 'under',
                                items: [
                                    {
                                        xtype: 'radio',
                                        itemId: 'uni-reading-quality-input-radio',
                                        boxLabel: Uni.I18n.translate('general.specifyReadingQuality', 'UNI', 'Specify reading quality'),
                                        name: 'rb',
                                        inputValue: '0',
                                        margin: '10 0 0 0',
                                        checked: true
                                    },
                                    {
                                        xtype: 'button',
                                        tooltip: Uni.I18n.translate('general.specifyReadingQuality.tooltip', 'UNI', "The wildcard '*' can be used in the 1st and 3rd part of the reading quality and will match all possible values."),
                                        iconCls: 'uni-icon-info-small',
                                        ui: 'blank',
                                        shadow: false,
                                        margin: '15 0 0 10',
                                        width: 16,
                                        tabIndex: -1
                                    }
                                ]
                            },
                            {
                                xtype: 'form',
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
                                itemId: 'des-eventtypeparts-input-radio',
                                boxLabel: Uni.I18n.translate('general.specifyReadingQualityParts', 'UNI', 'Specify reading quality parts'),
                                name: 'rb',
                                inputValue: '1'
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
                text: Uni.I18n.translate('general.add','UNI','Add'),
                ui: 'action',
                itemId: 'uni-reading-quality-add-button',
                listeners: {
                    click: {
                        fn: function () {
                            var radioGroup = this.up('#uni-add-reading-quality-window').down('#uni-reading-quality-input-method');
                            if (radioGroup.getValue().rb === '0') {
                                me.readingQualities.addReadingQuality(
                                    this.up('#uni-add-reading-quality-window').down('#uni-reading-quality-input-field').getValue()
                                );
                            } else {
                                me.readingQualities.addReadingQuality(
                                    this.up('#uni-add-reading-quality-window').down('#uni-system-combo').getValue()
                                        + '.' +
                                        this.up('#uni-add-reading-quality-window').down('#uni-category-combo').getValue()
                                        + '.' +
                                        this.up('#uni-add-reading-quality-window').down('#uni-index-combo').getValue()
                                );
                            }
                            this.up('#uni-add-reading-quality-window').destroy();
                        }
                    }
                }
            },
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel','UNI','Cancel'),
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

    onAfterRender: function() {
        var me = this,
            radioButton = me.down('#uni-reading-quality-input-radio'),
            inputField = me.down('#uni-reading-quality-input-field'),
            systemCombo = me.down('#uni-system-combo'),
            categoryCombo = me.down('#uni-category-combo'),
            indexCombo = me.down('#uni-index-combo');

        me.readingQualities.initializeStoreInformation();
        systemCombo.bindStore(me.readingQualities.getSystemStore(), true);
        categoryCombo.bindStore(me.readingQualities.getCategoryStore(), true);
        indexCombo.setDisabled(true);

        inputField.on("change", me.updateAddButton, me);
        radioButton.on("change", me.onRadioBtnChange, me);
        systemCombo.on("change", me.updateAddButton, me);
        categoryCombo.on("change", me.onCategoryComboChanged, me);
        indexCombo.on("change", me.updateAddButton, me);

        me.updateAddButton();
    },

    onRadioBtnChange: function(field, newValue, oldValue) {
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
        me.updateAddButton();
    },

    onCategoryComboChanged: function(combo, newValue, oldValue) {
        var me = this,
            indexCombo = me.down('#uni-index-combo');

        indexCombo.bindStore(me.readingQualities.getIndexStore(newValue), true);
        indexCombo.setValue(null);
        indexCombo.setDisabled(false);
        me.updateAddButton();
    },

    updateAddButton: function() {
        var me = this,
            radioGroup = me.down('#uni-reading-quality-input-method'),
            inputField = me.down('#uni-reading-quality-input-field'),
            systemCombo = me.down('#uni-system-combo'),
            categoryCombo = me.down('#uni-category-combo'),
            indexCombo = me.down('#uni-index-combo'),
            addButton = me.down('#uni-reading-quality-add-button');

        if (radioGroup.getValue().rb === '0') {
            var typedCimCode = inputField.getValue();
            addButton.setDisabled(
                Ext.isEmpty(typedCimCode) ||
                ! /^[0-9\*]+\.[0-9]+\.[0-9\*]+$/.test(typedCimCode)
            );
        } else {
            addButton.setDisabled(
                Ext.isEmpty(systemCombo.getValue()) ||
                Ext.isEmpty(categoryCombo.getValue()) ||
                Ext.isEmpty(indexCombo.getValue())
            );
        }
    }

});