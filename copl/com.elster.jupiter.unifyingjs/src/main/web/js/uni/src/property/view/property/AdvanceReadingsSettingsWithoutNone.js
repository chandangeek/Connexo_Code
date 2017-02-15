/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.AdvanceReadingsSettingsWithoutNone', {
    extend: 'Uni.property.view.property.Base',
    requires: [
        'Uni.form.field.ReadingTypeCombo'
    ],
    getEditCmp: function () {
        var me = this;
        return [
            {
                xtype: 'container',
                layout: 'vbox',
                items: [
                    {
                        xtype: 'radiogroup',
                        name: this.getName(),
                        itemId: 'readingRadioGroup',
                        width: me.width,
                        columns: 1,
                        vertical: true,
                        readOnly: me.isReadOnly,
                        fieldLabel: me.boxLabel ? me.boxLabel : '',
                        items: [
                            {boxLabel:  Uni.I18n.translate('advanceReadingProperty.bulkReading','UNI','Bulk reading'), name: 'advanceRb', inputValue: '2'},
                            {boxLabel: Uni.I18n.translate('general.readingType','UNI','Reading type'), name: 'advanceRb', inputValue: '3'}
                        ]
                    },
                    {
                        itemId: 'readingTypeCombo',
                        xtype: 'reading-type-combo',
                        name: 'readingTypeCombo',
                        msgTarget: 'under',
                        fieldLabel: '',
                        width: me.width,
                        valueField: 'mRID',
                        forceSelection: true,
                        store: this.readingTypes,

                        listConfig: {
                            cls: 'isu-combo-color-list',
                            emptyText: Uni.I18n.translate('general.readingtype.noreadingtypefound', 'UNI', 'No reading types found')
                        },

                        queryMode: 'remote',
                        queryParam: 'like',
                        queryDelay: 100,
                        queryCaching: false,
                        minChars: 1,
                        editable:true,
                        typeAhead:true,
                        emptyText: Uni.I18n.translate('general.readingtype.selectreadingtype', 'UNI', 'Start typing to select a reading type...')
                    }

                ]
            }
        ];
    },

    initListeners: function () {
        var me = this;
        if (me.down('#readingTypeCombo')) {
            me.down('#readingTypeCombo').on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
        me.callParent(arguments);
    },

    customHandlerLogic: function(){
        var field = this.getField();
        if(field.getValue().advanceRb==='2'){
            this.down('#readingTypeCombo').setDisabled(true);
        } else {
            this.down('#readingTypeCombo').setDisabled(false);
        }
    },

    getField: function () {
        return this.down('radiogroup');
    },

    setValue: function (value) {
        var me = this;
        if (!me.isEdit) {
            this.getDisplayField().setValue(this.getValueAsDisplayString(value));
        } else {
            if (value.bulk) {
                me.down('radiogroup').setValue({advanceRb:2});
                me.down('#readingTypeCombo').setDisabled(true);
            } else {
                me.down('radiogroup').setValue({advanceRb:3});
                var readingTypeStore = me.down('#readingTypeCombo').getStore();
                readingTypeStore.load({
                    params: {like: value.readingType && value.readingType.fullAliasName},
                    callback: function () {
                        me.down('#readingTypeCombo').setValue(value.readingType && value.readingType.mrid);
                    }
                });
            }
        }
    },

    getValue: function () {
        var me = this;
        if(me.down('#readingRadioGroup').getValue().advanceRb==='2'){
            return {
                bulk: true
            }
        } else {
            var value = me.down('#readingTypeCombo').getValue(),
                record = me.down('#readingTypeCombo').findRecordByValue(value),
                readingType = record ? record.getData() : { mRID: value };

            return {
                bulk: false,
                readingType: readingType
            }
        }
    },

    getDisplayCmp: function () {
        var me = this;
        return {
            xtype: 'displayfield',
            name: me.getName(),
            itemId: me.key + 'displayfield',
            width: me.width,
            msgTarget: 'under',
            renderer: function(value) {
                return Ext.isEmpty(value) ? '-' : value;
            }
        }
    },

    getDisplayField: function () {
        return this.down('displayfield');
    },

    initComponent: function(){
        this.readingTypes = Ext.create('Uni.property.store.PropertyReadingTypes');
        this.callParent(arguments);
    },

    doEnable: function(enable) {
        if (this.getField()) {
            if (enable) {
                this.getField().enable();
                this.customHandlerLogic();
            } else {
                this.getField().disable();
                this.down('#readingTypeCombo').disable();
            }
        }
    },

    getValueAsDisplayString: function (value) {
        if (Ext.isObject(value)) {
            if (value.bulk) {
                return Uni.I18n.translate('advanceReadingProperty.bulkReading', this.translationKey, 'Bulk reading');
            } else {
                return value.readingType.fullAliasName;
            }
        }
        return this.callParent(arguments);
    },

    markInvalid: function(errorMsg) {
        this.down('#readingTypeCombo').markInvalid(errorMsg);
    },

    clearInvalid: function() {
        this.down('#readingTypeCombo').clearInvalid();
    }

});