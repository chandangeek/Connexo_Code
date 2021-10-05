/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.property.view.property.BaseCombo
 * @abstract
 *
 * This is base class for property which can be a combo (weird, better have separate propery for combo).
 * Do not use this class directly!
 *
 * @see: Uni.property.view.property.Text for example of custom property implementation
 */
Ext.define('Uni.property.view.property.BaseCombo', {
    extend: 'Uni.property.view.property.Base',

    /**
     * @final
     * @returns {Object}
     */
    getEditCmp: function () {
        return this.isCombo() ? this.getComboCmp() : this.getNormalCmp();
    },

    /**
     * Return is component a combobox or not
     *
     * @returns {boolean}
     */
    isCombo: function () {
        return this.getProperty().getSelectionMode() === 'COMBOBOX';
    },

    /**
     * @abstract
     *
     * You must implement this method on inheritance
     */
    getNormalCmp: function() {
        throw 'getNormalCmp is not implemented';
    },

    getComboCmp: function () {
        var me = this,
            sortedStore = me.getProperty().getPossibleValues().sort(me.getSortFunctionForPossibleValues()),
            propertyValue = me.getProperty().get('value');
        return {
            xtype: 'combobox',
            itemId: me.key + 'combobox',
            name: this.getName(),
            store: sortedStore,
            queryMode: 'local',
            // We want typeAhead enabled, but if combo is not editable,
            // typeAhead must be disabled
            typeAhead: this.isEditable(),
            autoSelect: true,
            displayField: 'value',
            valueField: 'key',
            value: (!propertyValue ? undefined : propertyValue),
            width: me.width,
            readOnly: me.isReadOnly,
            allowBlank: !me.getProperty().data.required,
            blankText: me.blankText,
            forceSelection: this.getProperty().isEditable() || me.getProperty().getExhaustive(),
            editable: me.isEditable(),
            emptyText: this.getProperty().isEditable() === false ? '' : Uni.I18n.translate('general.selectValue', 'UNI', 'Select a value ...'),
            listConfig: {
                loadMask: true,
                maxHeight: 300
            }
        }
    },

    isEditable: function () {
        this.getProperty().isEditable() || !this.getProperty().getExhaustive();
    },

    setValue: function (value) {
        if (this.isEdit) {
            this.isCombo()
                ? this.getComboField().setValue(value)
                : this.callParent(arguments);
        } else {
            this.callParent(arguments);
        }
    },

    getComboField: function () {
        return this.down('combobox');
    },

    markInvalid: function (error) {
        this.down('combobox').markInvalid(error);
    },

    clearInvalid: function (error) {
        this.down('combobox') && this.down('combobox').clearInvalid();
    },

    initListeners: function () {
        var me = this;
        this.callParent(arguments);
        var field = me.getComboField();

        if (field) {
            field.on('change', function () {
                me.getProperty().set('isInheritedOrDefaultValue', false);
                me.updateResetButton();
            });
        }
    },

    doEnable: function(enable) {
        const comboField = this.getComboField();
        if (this.getField()) {
            if (this.isCombo() && comboField !== null) {
                if (enable) {
                    comboField.enable();
                } else {
                    comboField.disable();
                }
            } else {
                this.callParent(arguments);
            }
        }
    },

    // Override if you want a sorting other than alphabetically (eg. cf. Number.js)
    getSortFunctionForPossibleValues: function() {
        return undefined; // = alphabetically by default
    }
});