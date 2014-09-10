/**
 * @class Uni.override.FieldSetOverride
 */
Ext.define('Uni.override.FieldSetOverride', {
    override: 'Ext.form.FieldSet',

    initComponent: function () {
        this.callParent();
        this.form = new Ext.form.Basic(this);
        this.form.monitor.selector = "[isFormField]:not([excludeForm]){ownerCt.getId() === '" + this.getId() + "'}";
    },

    getValues: function () {
        var values = this.form.getValues();
        _.each(this.items.items, function (item) {
            if (_.isFunction(item.getValues)) {
                _.isEmpty(item.name) ? Ext.merge(values, item.getValues()) : values[item.name] = item.getValues();
            }
        });
        return values;
    },

    setValues: function (data) {
        this.form.setValues(data);
        _.each(this.items.items, function (item) {
            if (_.isFunction(item.setValues)) {
                _.isEmpty(item.name) ? item.setValues(data) : item.setValues(data[item.name]);
            }
        });
    }
});