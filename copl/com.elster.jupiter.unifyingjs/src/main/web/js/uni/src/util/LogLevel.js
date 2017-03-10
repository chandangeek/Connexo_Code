/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.util.LogLevel', {
    singleton: true,

    requires: [
        'Uni.store.LogLevels'
    ],

    emptyText: '-',
    logLevelsStore : Ext.getStore('LogLevelsStore'),
    logLevelIds: undefined,

    loadLogLevels: function(callbackFunction) {
        var me = this;
        if (Ext.isEmpty(me.logLevelsStore)) {
            me.logLevelsStore = Ext.create('Uni.store.LogLevels');
        }
        if (me.logLevelsStore.getCount()===0) {
            me.logLevelsStore.load(function(){
                if (Ext.isFunction(callbackFunction)) {
                    callbackFunction();
                }
            });
        }
    },

    getLogLevel: function(level, field) {
        var me = this,
            resultingLogLevel = undefined;

        if (Ext.isEmpty(me.logLevelsStore)) {
            me.logLevelsStore = Ext.create('Uni.store.LogLevels');
        }
        if (me.logLevelsStore.getCount()===0) {
            me.logLevelsStore.on('load', function() {
                if (Ext.isDefined(field) && field.xtype === 'log-level-displayfield') {
                    field.setValue(field.getValue()); // = trigger the rendering once again
                }
            }, me, {single: true});
            me.logLevelsStore.load();
            return me.emptyText; // show the empty text while waiting for the store loading
        } else {
            if (Ext.isEmpty(me.logLevelIds)) {
                me.logLevelIds = [];
                me.logLevelsStore.each(function (logLevel) {
                    me.logLevelIds.push(logLevel.get('id'));
                }, me);
                Ext.Array.sort(me.logLevelIds, function (logLevelId1, logLevelId2) {
                    return logLevelId1 - logLevelId2;
                });
            }
            Ext.Array.forEach(me.logLevelIds, function (logLevelId) {
                if (logLevelId <= level) {
                    resultingLogLevel = logLevelId;
                }
            });
            if (Ext.isEmpty(resultingLogLevel)) {
                return me.emptyText;
            }
            var storeIndex = me.logLevelsStore.findExact('id', resultingLogLevel);
            return storeIndex === -1 ? me.emptyText : me.logLevelsStore.getAt(storeIndex).get('displayValue');
        }
    }

});