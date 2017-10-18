/**
 * Created by H251853 on 9/14/2017.
 */
Ext.define('Mdc.store.device.IssuesAlarmsReasons', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.IssuesAlarmsReason'
    ],
    pageSize: undefined,
    sorters: [{
        property: 'issueType',
        direction: 'ASC'
    },
        {
            property: 'name',
            direction: 'ASC'
        }
    ],
    model: 'Mdc.model.IssuesAlarmsReason',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/1/history/issueandalarmreasons',
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});