Ext.define('Mdc.store.TimeUnitsWithoutMilliseconds', {
    extend: 'Mdc.store.TimeUnits',
    listeners: {
        load: function () {
            var index = this.find('timeUnit', 'milliseconds');

            if (index !== -1) {
                this.remove(this.getAt(index));
            }
        }
    }
});