/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.TaskInfo', {
    extend: 'Ext.data.Model',
    fields: [
        'id', 'application', 'name', 'type',
        {
            name: 'displayType',
            persist: false
        },
        {
            name: 'displayName',
            persist: false,
            convert: function (value, rec) {
                return Ext.String.format(Uni.I18n.translate('task.taskType', 'APR', '{0} ({1})', [rec.get('name'), rec.get('displayType')]));
            }
        }
    ]
});
