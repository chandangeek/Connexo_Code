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
        'Uni.property.controller.Registry'
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

    /**
     * Initialises form, creates form field based on properties specification in property registry:
     * @see Uni.property.controller.Registry
     *
     * @param {MixedCollection} properties
     */
    initProperties: function(properties) {
        var me = this;
        var registry = Uni.property.controller.Registry;

        me.removeAll();
        properties.each(function (property) {
            if (!(property instanceof Uni.property.model.Property)) {
                throw '!(entry instanceof Uni.property.model.Property)';
            }

            property.initValues();
            properties.commitChanges();

            var type = property.getType();
            var fieldType = registry.getProperty(type);
            if (fieldType) {
                var field = Ext.create(fieldType, Ext.apply(me.defaults, {
                    property: property,
                    isEdit: me.isEdit
                }));

                me.add(field);
            }
        });

        this.initialised = true;
    },

    getFieldValues: function(dirtyOnly) {
        var data = this.getValues(false, dirtyOnly, false, true);
        return this.unFlattenObj(data);
    },

    updateRecord: function() {
        var me = this;
        var raw = me.getFieldValues();
        var values = [];
        _.each(raw.properties || [], function(rawValue, key){
            var field = me.getPropertyField(key);
            values[key] = field.getValue(rawValue);
        });

        this.getForm().hydrator.hydrate(values, me.getRecord());
    },

    unFlattenObj: function(object) {
        return _(object).inject(function(result, value, keys) {
            var current = result,
                partitions = keys.split('.'),
                limit = partitions.length - 1;

            _(partitions).each(function(key, index) {
                current = current[key] = (index == limit ? value : (current[key] || {}));
            });

            return result;
        }, {});
    },

    /**
     * Updates the form with the new properties data
     *
     * @param {MixedCollection} properties
     */
    setProperties: function(properties) {
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

    restoreAll: function() {
        this.items.each(function(item){
            item.restoreDefault();
        })
    },

    /**
     * Returns property field by property model
     * @param {string} key
     * @returns {Uni.property.view.property.Base}
     */
    getPropertyField: function(key) {
        return this.getComponent(key);
    }
});