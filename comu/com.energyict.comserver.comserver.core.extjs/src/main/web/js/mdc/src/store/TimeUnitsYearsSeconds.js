/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.store.TimeUnitsYearsSeconds', {
    extend: 'Mdc.store.TimeUnits',
    storeId: 'TimeUnitsYearsSeconds',
    listeners: {
        load: function () {
            var index = this.find('timeUnit', 'milliseconds');

            if (index !== -1) {
                this.remove(this.getAt(index));
            }

            index = this.find('timeUnit', 'seconds');
            if (index !== -1) {
                this.remove(this.getAt(index));
            }

            index = this.find('timeUnit', 'hours');
            if (index !== -1) {
                this.remove(this.getAt(index));
            }

            index = this.find('timeUnit', 'minutes');
            if (index !== -1) {
                this.remove(this.getAt(index));
            }

            index = this.find('timeUnit', 'days');
            if (index !== -1) {
                this.remove(this.getAt(index));
            }

            index = this.find('timeUnit', 'weeks');
            if (index !== -1) {
                this.remove(this.getAt(index));
            }

        }
    }
});