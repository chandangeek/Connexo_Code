/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.util.LogLevel', {
    singleton: true,

    requires: [
        'Ldr.store.LogLevels'
    ],

    emptyText: '-',
    logLevelsStore: Ldr.store.LogLevels,
    logLevelIds: undefined,

    /**
     * Looks up the (human readable) log level to display for the given level (number)
     *
     * @param level A number representing the log level
     */
    getLogLevel: function(level) {
        var me = this,
            resultingLogLevel = undefined;

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

});