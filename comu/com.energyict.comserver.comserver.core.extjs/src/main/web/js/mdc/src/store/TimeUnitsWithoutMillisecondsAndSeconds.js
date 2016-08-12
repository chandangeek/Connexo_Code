Ext.define('Mdc.store.TimeUnitsWithoutMillisecondsAndSeconds', {
    extend: 'Mdc.store.TimeUnits',
    storeId: 'TimeUnitsWithoutMillisecondsAndSeconds',
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

        }
    }
});