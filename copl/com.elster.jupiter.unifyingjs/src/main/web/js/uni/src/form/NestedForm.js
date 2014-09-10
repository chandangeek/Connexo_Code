/**
 * @class Uni.form.NestedForm
 */
Ext.define('Uni.form.NestedForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.nested-form',

    initComponent: function () {
        this.callParent();
        this.getForm().monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.callParent();
        _.each(this.items.items, function (item) {
            if (_.isFunction(item.getValues)) values[item.name] = item.getValues();
        });
        return values;
    },

    loadRecord: function (record) {
        this.callParent(arguments);
        _.each(this.items.items, function (item) {
            if (!_.isEmpty(item.name) && _.has(record.getData(), item.name)) {
                if (_.isFunction(item.setValues) && _.isObject(record.getData()[item.name])) {
                    item.setValues(record.getData()[item.name]);
                }
            }
        });
    }
});