/**
 * @class Uni.util.Filters
 *
 * This class contains filters functions.
 */
Ext.define('Uni.util.Filters', {
    singleton: true,

    createFiltersObject: function (params) {
        var result = [];

        for (var dataIndex in params) {
            var value = params[dataIndex];

            if (params.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                var filter = {
                    property: dataIndex,
                    value: value
                };

                result.push(filter);
            }
        }

        return Ext.encode(result);
    },

    getFilterParams: function (filters, includeUndefined, flattenObjects) {
        var me = this,
            params = {};

        includeUndefined = includeUndefined || false;
        flattenObjects = flattenObjects || false;

        filters.each(function (filter) {
            if (!flattenObjects && Ext.isDefined(filter.applyParamValue)) {
                filter.applyParamValue(params, includeUndefined, flattenObjects);
            } else {
                var dataIndex = filter.dataIndex,
                    paramValue = filter.getParamValue();

                if (!includeUndefined && Ext.isDefined(paramValue) && !Ext.isEmpty(paramValue)) {
                    params[dataIndex] = paramValue;
                } else {
                    params[dataIndex] = paramValue;
                }

                if (flattenObjects && Ext.isObject(paramValue)) {
                    me.populateParamsFromObject(params, dataIndex, paramValue);
                }
            }
        }, me);

        return params;
    },

    populateParamsFromObject: function (params, dataIndex, paramValue) {
        var me = this;

        for (var index in paramValue) {
            var value = paramValue[index];

            if (paramValue.hasOwnProperty(index) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                if (Ext.isObject(value)) {
                    me.populateParamsFromObject(params, dataIndex + '.' + index, value);
                } else {
                    params[dataIndex + '.' + index] = value;
                    delete params[dataIndex];
                }
            }
        }
    },

    updateHistoryState: function (filters) {
        var me = this,
            params = me.getFilterParams(filters, true, true),
            href = Uni.util.QueryString.buildHrefWithQueryString(params, false);

        if (location.href !== href) {
            Uni.util.History.suspendEventsForNextCall();
            location.href = href;
        }
    },

    hasActiveFilter: function (filters) {
        var result = false;

        filters.each(function (filter) {
            if (filter.active) {
                result = true;
                return false;
            }
        });

        return result;
    },

    loadHistoryState: function (filters, ignoreNesting) {
        var me = this,
            queryObject = Uni.util.QueryString.getQueryStringValues(false),
            objectQueue = {};

        for (var dataIndex in queryObject) {
            var value = queryObject[dataIndex];

            if (queryObject.hasOwnProperty(dataIndex) && Ext.isDefined(value) && !Ext.isEmpty(value)) {
                if (!ignoreNesting && dataIndex.indexOf('.') >= 0) {
                    var tempQueue = {};
                    me.addValueToObj(tempQueue, dataIndex, value);
                    Ext.merge(objectQueue, tempQueue);
                } else {
                    me.setValueForDataIndex(filters, dataIndex, value);
                }
            }
        }

        // Set object values.
        for (var objectIndex in objectQueue) {
            var objectValue = objectQueue[objectIndex];
            if (objectQueue.hasOwnProperty(objectIndex) && Ext.isDefined(objectValue) && !Ext.isEmpty(objectValue)) {
                me.setValueForDataIndex(filters, objectIndex, objectValue);
            }
        }
    },

    addValueToObj: function (obj, index, value) {
        // Separate each step in the "path".
        var path = index.split(".");

        // Loop through each part of the path adding to obj.
        for (var i = 0, tmp = obj; i < path.length - 1; i++) {
            tmp = tmp[path[i]] = {};
        }

        // At the end of the chain add the value in.
        tmp[path[i]] = value;
    },

    setValueForDataIndex: function (filters, dataIndex, value) {
        var me = this;

        filters.each(function (filter) {
            if (filter.dataIndex === dataIndex) {
                filter.setFilterValue(value);
                return false;
            }
        }, me);
    }
});