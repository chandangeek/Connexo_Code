Ext.define('Imt.usagepointhistory.model.LifeCycleAndState', {
    extend: 'Ext.data.Model',
    fields: [
        'id',
        'type',
        'lifeCycle',
        'fromStateName',
        'toStateName',
        'microActions',
        'microChecks',
        'user',
        'status',
        'usagePoint',
        'userCanManageRequest',
        {name: 'transitionTime', dateFormat: 'time', type: 'date'},
        {
            name: 'type_name',
            mapping: function (data) {
                if (data.type) {
                    return data.type.displayValue;
                }
            }
        },
        {
            name: 'status_name',
            mapping: function (data) {
                if (data.status) {
                    return data.status.displayValue;
                }
            }
        },
        {
            name: 'user_name',
            mapping: function (data) {
                if (data.user) {
                    return data.user.displayValue;
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/upl/usagepoint/{usagePointId}/transitions/history',
        reader: {
            type: 'json'
        }
    }
});

