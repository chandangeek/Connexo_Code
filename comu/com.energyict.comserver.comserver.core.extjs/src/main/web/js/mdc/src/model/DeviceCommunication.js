Ext.define('Mdc.model.DeviceCommunication', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'},
        {name: 'comTask', type: 'auto', defaultValue: undefined},
        {name: 'comScheduleName', type: 'string'},
        {name: 'comScheduleFrequency', type: 'auto', defaultValue: undefined},
        {name: 'urgency', type: 'number'},
        {name: 'currentState', type: 'auto', defaultValue: undefined},
        {name: 'latestResult', type: 'auto', defaultValue: undefined},
        {name: 'startTime', type: 'date', dateFormat: 'time'},
        {name: 'successfulFinishTime', type: 'date', dateFormat: 'time'},
        {name: 'plannedDate', type: 'date', dateFormat: 'time'},
        {name: 'connectionMethod', type: 'string'},
        {name: 'connectionStrategy', type: 'auto', defaultValue: undefined},
        {name: 'isOnHold', type: 'boolean'},

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

    activate: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/activate'.replace('{id}', this.getId()),
            success: callback
        });
    },

    deactivate: function(callback) {
        Ext.Ajax.request({
            method: 'PUT',
            url: this.proxy.url + '/{id}/deactivate'.replace('{id}', this.getId()),
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
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }
});

