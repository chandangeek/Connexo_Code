Ext.define('Dlc.devicelifecycletransitions.view.widget.StatePropertiesForm', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.state-properties-form',

    propertiesStore: null,
    itemStack: 'default',

    parseByGroups: function (store) {
        var groupedItems = {},
            groupedIds = {},
            subcategoryItems = [],
            subCategoryIds = {},
            mergedSubcategories = {},
            mergedSubcategoryItems = [];

        if (!Ext.isEmpty(store)) {
            store.each(function (item) {
                if (Ext.isEmpty(item.get('conflictGroup'))) {
                    if (Ext.isEmpty(groupedItems[item.get('category').id])) {
                        groupedItems[item.get('category').id] = [item.getData()];
                        groupedIds[item.get('category').id] = item.get('category').name
                    } else {
                        groupedItems[item.get('category').id].push(item.getData());
                    }
                } else {
                    subcategoryItems.push(item);
                }
            });

            Ext.each(subcategoryItems, function (item) {
                if (Ext.isEmpty(mergedSubcategories[item.get('conflictGroup').id])) {
                    mergedSubcategories[item.get('conflictGroup').id] = [item];
                    subCategoryIds[item.get('conflictGroup').id] = item.get('conflictGroup').name
                } else {
                    mergedSubcategories[item.get('conflictGroup').id].push(item);
                }
            });

            Ext.iterate(subCategoryIds, function (key, value) {
                if (mergedSubcategories[key].length === 1) {
                    mergedSubcategoryItems.push(mergedSubcategories[key][0].getData());
                } else {
                    mergedSubcategoryItems.push(
                        {
                            isRequired: mergedSubcategories[key][0].get('isRequired'),
                            category: mergedSubcategories[key][0].get('category'),
                            description: mergedSubcategories[key][0].get('conflictGroup').description,
                            key: key,
                            name: value,
                            checked: false,
                            radioGroup: mergedSubcategories[key]
                        });
                }
            });
        }

        if (!Ext.isEmpty(mergedSubcategoryItems)) {
            Ext.each(mergedSubcategoryItems, function (item) {
                if (Ext.isEmpty(groupedItems[item.category.id])) {
                    groupedItems[item.category.id] = [item];
                    groupedIds[item.category.id] = item.category.name
                } else {
                    groupedItems[item.category.id].push(item);
                }
            });
        }

        return {items: groupedItems, ids: groupedIds}
    },

    createFieldForItem: function (item) {
        var me = this,
            radioItems = [],
            radioIsChecked = false,
            checkboxListeners = {},
            isRadioChecker = false,
            checkBoxCheck = item.checked,
            checkBoxInfo = item.description,
            additionalMessage,
            container;

        if (!Ext.isEmpty(item.radioGroup)) {
            isRadioChecker = true;

            Ext.each(item.radioGroup, function (radioBtn) {
                if (radioBtn.get('checked')) {
                    radioIsChecked = true
                }
                radioItems.push(
                    {
                        boxLabel: radioBtn.get('name'),
                        name: radioBtn.get('conflictGroup').id,
                        inputValue: radioBtn.get('key'),
                        checked: radioBtn.get('checked')
                    }
                )
            });

            checkBoxCheck = radioIsChecked;

            checkboxListeners = {
                change: function (combo, value) {
                    if (value) {
                        combo.up('container[name=mainContainer]').down('radiogroup').enable();
                    } else {
                        combo.up('container[name=mainContainer]').down('radiogroup').disable();
                    }
                }
            }
        }

        if (item.isRequired) {
            switch (me.itemStack) {
                case 'actions':
                    additionalMessage = Uni.I18n.translate('deviceLifeCycleTransitions.add.partOfMandatoryAction', 'DLC', 'This is a part of the mandatory auto actions.');
                    break;
                case 'checks':
                    additionalMessage = Uni.I18n.translate('deviceLifeCycleTransitions.add.partOfMandatoryChecks', 'DLC', 'This is a part of the mandatory checks.');
                    break;
            }
            checkBoxInfo = checkBoxInfo + " " + additionalMessage;
        }

        container = {
            xtype: 'container',
            name: 'mainContainer',
            layout: 'vbox',
            required: item.isRequired,
            categoryId: item.category.id,
            fieldGroup: me.itemStack + '_fieldItem',
            radioGroup: item.radioGroup,
            items: [
                {
                    xtype: 'container',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            itemId: item.key,
                            boxLabel: item.name,
                            checked: checkBoxCheck,
                            disabled: item.isRequired,
                            listeners: checkboxListeners,
                            isRadioChecker: isRadioChecker
                        },
                        {
                            xtype: 'button',
                            tooltip: checkBoxInfo,
                            margin: '5 0 0 0',
                            iconCls: 'uni-icon-info-small',
                            cls: 'uni-btn-transparent',
                            style: {
                                display: 'inline-block',
                                "text-decoration": 'none !important'
                            }
                        }
                    ]
                }
            ]
        };

        if (!Ext.isEmpty(radioItems)) {
            var radioGroup = {
                xtype: 'radiogroup',
                margin: '0 0 0 10',
                columns: 1,
                disabled: !radioIsChecked,
                vertical: true,
                items: radioItems
            };

            if (!radioIsChecked) {
                radioGroup.items[0].checked = true;
            }

            container.items.push(radioGroup);
        }

        return container;
    },

    showOptionalHandler: function (btn) {
        var me = this,
            fieldItems = me.getFieldItems(),
            visibleCategories = [];

        btn.disable();
        me.down('#showAllBtn').enable();
        if (fieldItems) {
            Ext.each(fieldItems, function (item) {
                if (item.required) {
                    item.hide();
                    me.down('#' + item.categoryId).hide();
                } else {
                    visibleCategories.push(item.categoryId)
                }
            });
        }

        Ext.each(visibleCategories, function (categoryId) {
            me.down('#' + categoryId).show();
        });
    },

    showAllHandler: function (btn) {
        var me = this,
            fieldItems = me.getFieldItems();

        btn.disable();
        me.down('#showOptionalBtn').enable();
        if (fieldItems) {
            Ext.each(fieldItems, function (item) {
                item.show();
                me.down('#' + item.categoryId).show();
            });
        }
    },

    getFieldItems: function () {
        return  Ext.ComponentQuery.query('[fieldGroup=' + this.itemStack + '_fieldItem]');
    },

    getFieldById: function (id) {
        return this.down('#' + id);
    },

    setValue: function (micro) {
        var me = this;
        Ext.each(micro, function (item) {
            if (Ext.isEmpty(item.conflictGroup)) {
                var field = me.getFieldById(item.key);
                if (field) {
                    field.setValue(item.checked);
                }
            } else {
                var combo = me.getFieldById(item.conflictGroup.id),
                    radiogroupValue = {};
                if (combo) {
                    radiogroupValue[item.conflictGroup.id] = item.key;
                    combo.setValue(true);

                    combo.up('container[name=mainContainer]').down('radiogroup').setValue(radiogroupValue);
                } else {
                    var field = me.getFieldById(item.key);
                    if (field) {
                        field.setValue(item.checked);
                    }
                }

            }
        })
    },

    getValue: function () {
        var fieldItems = this.getFieldItems(),
            values = [];

        Ext.each(fieldItems, function (item) {
            var box = item.down('checkbox');
            if (box.isRadioChecker) {
                var radioGroup = item.down('radiogroup'),
                    checkedKey = radioGroup.getValue()[box.itemId];

                Ext.each(item.radioGroup, function (radioItem) {
                    values.push({
                        key: radioItem.get('key'),
                        checked: box.getValue() && radioItem.get('key') === checkedKey
                    });
                })
            } else {
                values.push({
                    key: box.itemId,
                    checked: box.getValue()
                });
            }
        });

        return values;
    },

    initComponent: function () {
        var me = this,
            parsedProperties = me.parseByGroups(me.propertiesStore),
            groupedProperties = parsedProperties.items,
            groupedIds = parsedProperties.ids,
            isAllOptional = true,
            noItemsMessage;

        if (me.propertiesStore.count() > 0) {

            me.items = [
                {
                    xtype: 'container',
                    layout: 'hbox',
                    itemId: 'show-btns-container',
                    margin: '-2 0 20 0',
                    defaults: {
                        xtype: 'button',
                        ui: 'link'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('deviceLifeCycleTransitions.add.showOptional', 'DLC', 'Show optional only'),
                            itemId: 'showOptionalBtn',
                            scope: me,
                            handler: me.showOptionalHandler
                        },
                        {
                            xtype: 'container',
                            html: '<b>|</b>',
                            margin: '7 2 0 -10',
                            disabled: true
                        },
                        {
                            text: Uni.I18n.translate('deviceLifeCycleTransitions.add.showAll', 'DLC', 'Show all'),
                            itemId: 'showAllBtn',
                            scope: me,
                            handler: me.showAllHandler
                        }
                    ]
                }
            ];

            Ext.iterate(groupedProperties, function (key, items) {
                var groupItems = [
                    {
                        xtype: 'container',
                        html: '<h3>' + groupedIds[key] + '</h3>',
                        margin: '6 0 5 0'
                    }
                ];

                Ext.each(items, function (item) {
                    if (item.isRequired) {
                        isAllOptional = false;
                    }
                    var itemField = me.createFieldForItem(item);
                    groupItems.push(itemField);
                });

                me.items.push({
                    xtype: 'container',
                    items: groupItems,
                    itemId: key,
                    margin: '0 0 20 0'

                })
            });

            me.callParent(arguments);

            if (isAllOptional) {
                me.down('#show-btns-container').setVisible(false);
            } else {
                var optionalBtn = me.down('#showOptionalBtn');
                me.showOptionalHandler(optionalBtn);
            }

        } else {
            switch (me.itemStack) {
                case 'actions':
                    noItemsMessage = Uni.I18n.translate('deviceLifeCycleTransitions.add.noMandatoryAction', 'DLC', 'No auto actions are available.');
                    break;
                case 'checks':
                    noItemsMessage = Uni.I18n.translate('deviceLifeCycleTransitions.add.noMandatoryChecks', 'DLC', 'No pretransition checks are available.');
                    break;
            }

            me.items = [
                {
                    xtype: 'container',
                    margin: '-5 0 0 0',
                    html: '<span style="font-style:italic;color: grey;">' + noItemsMessage + '</span>'
                }
            ];

            me.callParent(arguments);
        }

    }
});


