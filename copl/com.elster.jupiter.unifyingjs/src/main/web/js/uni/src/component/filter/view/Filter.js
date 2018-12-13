/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.component.filter.view.Filter
 * @deprecated
 *
 * Filter panel is an extension over Ext js form panel See {@link Ext.form.Panel}.
 *
 * Filter panel fixes form data binding (loading data from model to the form and update model from form's data).
 * Panel allows you to bind models that contains associations (hasOne, hasMany). Note that form should contain
 * components with the binded stores (See: {@link Ext.util.Bindable}) to properly fetch data between models and have name
 * the same as association name.
 *
 * @Example
 *
 *    Ext.define('App.view.Filter', {
 *      extend: 'Uni.component.filter.view.Filter',
 *      alias: 'widget.filter',
 *      title: 'Filter',
 *
 *      items: [{
 *          xtype: 'combobox',
 *          name: 'reason',
 *          fieldLabel: 'Reason',
  *         displayField: 'name',
 *          valueField: 'id',
 *          store: 'App.store.Reason',
 *      }]
 *   });
 *
 * Ext.define('App.model.IssueFilter', {
 *     extend: 'Uni.component.filter.model.Filter',
 *
 *     requires: [
 *         'App.model.Reason'
 *     ],
 *
 *     hasOne: [{
 *         model: 'App.model.Reason',
 *         associationKey: 'reason',
 *         name: 'reason'
 *     }],
 * });
 *
 * // Now model App.model.IssueFilter can be binded to Filter panel as usual:
 *
 * var model = new App.model.IssueFilter();
 * var filter = new App.view.Filter();
 *
 * filter.loadRecord(model);
 *
 * // After form chandes by user you can update binded filter model by calling^
 *
 * var filterModel = form.getRecord();
 * form.updateRecord(filterModel);
 *
 */
Ext.define('Uni.component.filter.view.Filter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.filter-form',
    applyKey: 13,
    /**
     * @override
     *
     * Load data to the form of the model with associations.
     *
     * @param filter Uni.component.filter.model.Filter
     * @returns {Ext.form.Basic}
     */
    loadRecord: function(filter) {
        var me = this,
            data = filter.getData(true);

        this.callParent([filter]);
        filter.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    data[association.name] = me.extractHasOne(filter[association.getterName].call(filter));
                    break;
                case 'hasMany':
                    data[association.name] = me.extractHasMany(filter[association.name]());
                    break;
            }
        });

        return this.getForm().setValues(data);
    },

    /**
     * Extracts data from the association object to the Integer
     *
     * @param record The associated record
     *
     * @returns {Number}
     */
    extractHasOne: function (record) {
        return record ? record.getId() : null;
    },

    /**
     * Extracts data from the store to the array
     *
     * @param store The associated store
     *
     * @returns {Number[]}
     */
    extractHasMany: function (store) {
        var result = [];

        store.each(function(record){
            result.push(record.getId());
        });

        return result;
    },

    /**
     * @override
     *
     * Extracts data from the form and bind to the filter model
     *
     * @param record The associated filter record
     * @returns {Uni.component.filter.view.Filter}
     */
    updateRecord: function (record) {
        this.callParent([record]);
        var me = this,
            values = this.getValues();

        record = record || this._record;
        record.associations.each(function (association) {
            switch (association.type) {
                case 'hasOne':
                    me.hydrateHasOne(record, association, values);
                    break;
                case 'hasMany':
                    me.hydrateHasMany(record, association, values);
                    break;
            }
        });

        return this;
    },

    /**
     * Hydrates array data to the associated model field
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasOne: function (record, association, values) {
        var name = association.name,
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            record[association.setterName](Ext.create(association.model));
        } else if (cmp && cmp.mixins.bindable) {
            var store = cmp.getStore();
            var rec = store.getById(values[name]);
            record[association.setterName](rec);
        }

        return this;
    },

    /**
     * Hydrates array data to the associated model store
     *
     * @param record Filter model
     * @param association selected association
     * @param values data array
     */
    hydrateHasMany: function (record, association, values) {
        var name = association.name,
            store = record[name](),
            cmp = this.down('[name="' + name + '"]');

        if (!values[name]) {
            store.removeAll();

        } else if (cmp && cmp.mixins.bindable && values[name]) {
            var cmpStore = cmp.getStore();

            if (!_.isArray(values[name])) {
                values[name] = [values[name]];
            }

            var records = _.map(values[name], function (value) {
                return cmpStore.getById(value);
            });
            store.loadRecords(records, {});
        }

        return this;
    },

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.on('afterrender', function (form) {
            var el = form.getEl();
            el.on('keypress', function (e, t) {
                (e.getKey() == me.applyKey) && (me.fireEvent('applyfilter', {me: me, key: me.applyKey, t: t}));
            })
        })
    }
});