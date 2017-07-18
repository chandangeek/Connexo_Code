/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.TimeUnitsWithoutMilliseconds', {
    extend: 'Mdc.store.TimeUnits',
    storeId: 'TimeUnitsWithoutMilliseconds',
    listeners: {
        load: function () {
            var index = this.find('timeUnit', 'milliseconds');

            if (index !== -1) {
                this.remove(this.getAt(index));
            }
        }
    }
});