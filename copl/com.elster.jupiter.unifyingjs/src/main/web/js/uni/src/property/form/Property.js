/**
 * @class Uni.property.form.Property
 *
 * Properties form. used for display properties.
 * Usage example:
 *
 * // assume you have alredy specify property in view like {xtype: 'property-form'}
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
    isReadOnly: false,
    inheritedValues: false,
    inputType: 'text',
    passwordAsTextComponent: false,

    /**
     * Loads record to the form.
     * If form is not initialised performs initProperties()
     *
     * @param record
     */
    loadRecord: function (record) {
        this.initProperties(record.properties());
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
        var me = this;
        var registry = Uni.property.controller.Registry;

        me.removeAll();
        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            me.inheritedValues
                ? property.initInheritedValues()
                : property.initValues();

            properties.commitChanges();

            var type = property.getType();
            var fieldType = registry.getProperty(type);
            if (fieldType) {
                var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                    property: property,
                    isEdit: me.isEdit,
                    isReadOnly: me.isReadOnly,
                    inputType: me.inputType,
                    passwordAsTextComponent: me.passwordAsTextComponent
                }));

                me.add(field);
            }
        });

        this.initialised = true;
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

        me.getRecord().properties().each(function (property) {
            var key = property.get('key');
            var field = me.getPropertyField(key);
            values[key] = field.getValue(raw);
        });

        this.getForm().hydrator.hydrate(values, me.getRecord());
    },

    unFlattenObj: function (object) {
        return _.reduce(object, function (result, value, key) {
            var properties = key.split('.');
            result[_.first(properties)][_.rest(properties, 1).join('.')] = value;
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

    restoreAll: function () {
        this.items.each(function (item) {
            item.restoreDefault();
        })
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
    }
});