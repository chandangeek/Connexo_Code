/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.search.field.internal.ClockField', {
    extend: 'Uni.view.search.field.internal.DateTimeField',
    alias: 'widget.uni-search-internal-clock',

    getValue: function() {
        var value = this.callParent(arguments);

        return value ? value / 1000 : value;
    },

    setValue: function(value) {
        this.callParent([value * 1000]);
    }
});