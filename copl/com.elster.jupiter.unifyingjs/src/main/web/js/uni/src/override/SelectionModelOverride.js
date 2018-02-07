/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.ButtonOverride
 */
Ext.define('Uni.override.SelectionModelOverride', {
    override: 'Ext.selection.Model',
    suppressMultiSelEvent: false,
    /*

     suppress change event base on number of selected items
     */
    doMultiSelect: function (records, keepExisting, suppressEvent) {
        var me = this,
            selected = me.selected,
            change = false,
            result, i, len, record, commit;

        if (me.locked) {
            return;
        }

        records = !Ext.isArray(records) ? [records] : records;
        len = records.length;
        if (!keepExisting && selected.getCount() > 0) {
            result = me.deselectDuringSelect(records, selected.getRange(), suppressEvent);
            if (result[0]) {
                // We had a failure during seletion, so jump out
                // Fire selection change if we did deselect anything
                me.maybeFireSelectionChange(result[1] > 0 && !suppressEvent);
                return;
            }
        }

        commit = function () {
            selected.add(record);
            change = true;
        };

        for (i = 0; i < len; i++) {
            record = records[i];
            if (me.isSelected(record)) {
                continue;
            }
            me.lastSelected = record;

            me.suppressMultiSelEvent ? me.onSelectChange(record, true, selected.getCount() > 1 && me.suppressMultiSelEvent, commit) :
                me.onSelectChange(record, true, suppressEvent, commit);
        }
        if (!me.preventFocus) {
            me.setLastFocused(record, suppressEvent);
        }
        // fire selchange if there was a change and there is no suppressEvent flag
        me.maybeFireSelectionChange(change && !suppressEvent);
    }

});