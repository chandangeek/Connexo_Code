/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.property.form.Property
 *
 * Properties form. used for display properties.
 * Usage example:
 *
 * // assume you have already specified property in view like {xtype: 'property-form'}
 *
 * var form = cmp.down('property-form');
 *
 * // record must have properties() association specified
 * form.loadRecord(record);
 *
 * // You can redraw form with new properties set:
 * form.initProperties(record.properties());
 *
 * // or update current form values
 * form.loadRecord(record);
 * // or
 * form.setProperties(record.properties());
 */
Ext.define('Uni.property.form.Property', {
    extend: 'Ext.form.Panel',
    alias: 'widget.property-form',
    hydrator: 'Uni.property.form.PropertyHydrator',
    border: 0,
    requires: [
        'Uni.property.controller.Registry',
        'Uni.property.form.PropertyHydrator'
    ],
    defaults: {
        labelWidth: 250,
        resetButtonHidden: false
    },
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initialised: false,
    isEdit: true,
    isMultiEdit: false,
    isReadOnly: false,
    inheritedValues: false,
    inputType: 'text',
    passwordAsTextComponent: false,
    userHasEditPrivilege: true,
    userHasViewPrivilege: true,
    editButtonTooltip: Uni.I18n.translate('general.edit', 'UNI', 'Edit'),
    removeButtonTooltip: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
    blankText: Uni.I18n.translate('general.requiredField', 'UNI', 'This field is required'),
    context: null,

    /**
     * Loads record to the form.
     * If form is not initialised performs initProperties()
     *
     * @param record
     */
    loadRecord: function (record, requestUrl) {
        this.initProperties(record.properties(), requestUrl);
        this.callParent(arguments);
    },

    loadRecordAsNotRequired: function (record) {
        var properties = record.properties();
        _.each(properties.data.items, function (item) {
            item.set('required', false)
        });
        this.loadRecord(record);
    },

    /**
     * Initialises form, creates form field based on properties specification in property registry:
     * @see Uni.property.controller.Registry
     *
     * @param {MixedCollection} properties
     */
    initProperties: function (properties) {
        var me = this,
            registry = Uni.property.controller.Registry,
            type,
            fieldType,
            dependOnIsEdited,
            addPropertyToForm = true;

        Ext.suspendLayouts();

        me.removeAll();
        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            if (me.isEdit && property.get('canBeOverridden') !== null) {
                addPropertyToForm = property.get('canBeOverridden');
            }
            if (addPropertyToForm) {
                me.inheritedValues
                    ? property.initInheritedValues()
                    : property.initValues();

                property.commit(true, []);

                type = property.getType();
                fieldType = registry.getProperty(type);
                dependOnIsEdited = me.isMultiEdit && !me.isEdit;
                console.log("ADD PROPERTY!!!!!!!!!!",me.context);
                if ((dependOnIsEdited && property.isEdited) || (!dependOnIsEdited && fieldType)) {
                    var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                        parentForm: me,
                        property: property,
                        isEdit: me.isEdit,
                        isReadOnly: me.isReadOnly,
                        inputType: me.inputType,
                        passwordAsTextComponent: me.passwordAsTextComponent,
                        userHasEditPrivilege: me.userHasEditPrivilege,
                        userHasViewPrivilege: me.userHasViewPrivilege,
                        showEditButton: me.isMultiEdit,
                        resetButtonHidden: me.defaults.resetButtonHidden || me.isMultiEdit,
                        editButtonTooltip: me.editButtonTooltip,
                        removeButtonTooltip: me.removeButtonTooltip,
                        blankText: me.blankText,
                        propertyParams : property.getPropertyParams()
                    }));
                    me.add(field);
                    field.on('checkRestoreAll', function () {
                        me.fireEvent('showRestoreAllBtn', me.checkAllIsDefault());
                    });
                }
                me.fireEvent('showRestoreAllBtn', me.checkAllIsDefault());
            }
        });

        Ext.resumeLayouts(true);

        me.initialised = true;
    },

    useInheritedValues: function () {
        this.items.each(function (item) {
            item.useInheritedValue();
        });
        this.inheritedValues = true;
    },

    getFieldValues: function (dirtyOnly) {
        var data = this.getValues(false, dirtyOnly, false, true);
        return this.unFlattenObj(data);
    },

    updateRecord: function () {

        var me = this;
        var raw = me.getFieldValues();
        var values = {};
        if(me.getRecord()){
            me.getRecord().properties().each(function (property) {
                var key = property.get('key');
                var field = me.getPropertyField(key);
                if (field !== undefined) {
                    values[key] = field.getValue(raw);
                }
            });
            this.getForm().hydrator.hydrate(values, me.getRecord());
        }
    },

    unFlattenObj: function (object) {
        return _.reduce(object, function (result, value, key) {
            var properties = key.split('.');
            if (_.first(properties) == 'properties') {
                result[_.first(properties)][_.rest(properties, 1).join('.')] = value;
            }
            return result;
        }, {properties: {}});
    },

    /**
     * Updates the form with the new properties data
     *
     * @param {MixedCollection} properties
     */
    setProperties: function (properties) {
        var me = this;

        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            var field = me.getPropertyField(property.get('key'));
            if (field) {
                field.setProperty(property);
            }
        });
    },

    setPropertiesAndDisable: function(propertiesArray) {
        var me = this;

        Ext.Array.each(propertiesArray, function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            var field = me.getPropertyField(property.get('key'));
            if (field) {
                field.setProperty(property);
                field.setDisabled(true);
            }
        });
    },

    restoreAll: function () {
        this.items.each(function (item) {
            item.restoreDefault();
        })
    },


    checkAllIsDefault: function () {
        var me = this,
            isDefault = true;

        me.items.each(function (item) {
            if (!item.getProperty().get('isInheritedOrDefaultValue')) {
                isDefault = false;
            }
        });
        return isDefault;
    },

    showValues: function () {
        this.items.each(function (item) {
            item.showValue();
        })
    },

    hideValues: function () {
        this.items.each(function (item) {
            item.hideValue();
        })
    },

    /**
     * Returns property field by property model
     * @param {string} key
     * @returns {Uni.property.view.property.Base}
     */
    getPropertyField: function (key) {
        return this.getComponent(key);
    },

    clearInvalid: function () {
        this.items.each(function (item) {
            item.clearInvalid();
        });
    },

    markInvalid: function (errors) {
        var me = this;
        Ext.each(errors, function (error) {
            if (!!me.getPropertyField(error.id)) {
                me.getPropertyField(error.id).markInvalid(error.msg);
            }
        });
    },

    makeEditable: function(record){
        this.isEdit =true;
        this.loadRecord(record);
    },

    makeNotEditable: function(record){
        this.isEdit =false;
        this.loadRecord(record);
    }
});