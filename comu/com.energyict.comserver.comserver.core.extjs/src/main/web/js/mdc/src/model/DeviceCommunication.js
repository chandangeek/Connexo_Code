Ext.define('Mdc.model.DeviceCommunication', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'},
        {name: 'comTask', type: 'auto'},
        {name: 'comScheduleName', type: 'string'},
        {name: 'comScheduleFrequency', type: 'auto'},
        {name: 'urgency', type: 'number'},
        {name: 'currentState', type: 'auto'},
        {name: 'latestResult', type: 'auto'},
        {name: 'startTime', type: 'date', dateFormat: 'time'},
        {name: 'successfulFinishTime', type: 'date', dateFormat: 'time'},
        {name: 'plannedDate', type: 'date', dateFormat: 'time'},
        {name: 'connectionMethod', type: 'string'},
        {name: 'connectionStrategy', type: 'auto'},
        {name: 'isOnHold', type: 'string'},

        // mapped fields
        {name: 'frequency', type: 'string', mapping: function (data) {
            if (!data.comScheduleFrequency.every) {
                return ''
            }
            var every = data.comScheduleFrequency.every;
            return Uni.I18n.translatePlural('device.communications.frequency', parseInt(every.count), 'MDC', 'Every {0}') + ' '
            + Uni.I18n.translate('device.communications.frequency.' + every.timeUnit, 'MDC', every.timeUnit);
        }, persist: false}
    ],

    run: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/run'.replace('{id}', this.getId()),
            success: callback
        });
    },

    runNow: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/runnow'.replace('{id}', this.getId()),
            success: callback
        });
    },

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/communications',
        reader: {
            type: 'json',
            root: 'communications',
            totalProperty: 'total'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        }
    }
});

