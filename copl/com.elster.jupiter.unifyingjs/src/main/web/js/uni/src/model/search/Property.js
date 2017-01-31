/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.search.Property
 */
Ext.define('Uni.model.search.Property', {
    extend: 'Ext.data.Model',
    idProperty: 'name',
    isCached: false,
    fields: [
        {name: 'name', type: 'string'},
        {name: 'displayValue', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'factoryName', type: 'string'},
        {name: 'exhaustive', type: 'boolean'},
        {name: 'link', type: 'auto'},
        {name: 'selectionMode', type: 'string'},
        {name: 'visibility', type: 'string'},
        {name: 'constraints', type: 'auto'},
        {name: 'group', type: 'auto'},
        {name: 'affectsAvailableDomainProperties', type: 'boolean'},
        {
            name: 'groupId', type: 'int', convert: function (v, record) {
            return record.raw.group ? record.raw.group.id : null;
        }
        },
        {
            name: 'sticky', type: 'boolean', convert: function (v, record) {
            return record.raw.visibility === 'sticky';
        }
        },
        {
            name: 'linkHref', type: 'auto', convert: function (v, record) {
            var linkParams = record.raw.link,
                result = undefined;

            if(Ext.isDefined(linkParams)) {
                result = linkParams.href;
            }

            return result;
        }
        },
        {name: 'constraints', type: 'auto'},
        {name: 'values', type: 'auto'},
        {name: 'disabled', type: 'boolean', persist: false, defaultValue : false}
    ],

    hasMany: {
        model: 'Uni.model.search.PropertyValue',
        name: 'values',
        associationKey: 'values',
        storeConfig: {
            remoteFilter: true,
            proxy: {
                type: 'ajax',
                pageParam: undefined,
                startParam: undefined,
                limitParam: undefined,
                url: '',
                reader: {
                    type: 'json',
                    root: 'values'
                }
            }
        }
    },

    refresh: function(callback) {
        var me = this,
            store = me.values(),
            filters = store.filters.getRange(),
            options = {
                action: 'read',
                filters: filters,
                records: [me]
            },
            operation = new Ext.data.Operation(options);

        me.getProxy().url = me.get('linkHref');
        me.getProxy().doRequest(operation, function() {
            store.removeAll(true);

            if (me.get('exhaustive')) {
                store.getProxy().url = me.get('linkHref');
                store.loadRawData(me.get('values'));
                if (Ext.isEmpty(me.get('values'))) {
                    me.beginEdit();
                    me.set('disabled', true);
                    me.endEdit(true);
                }
            }

            me.isCached = true;
            callback ? callback() : null;
        });
    }
});