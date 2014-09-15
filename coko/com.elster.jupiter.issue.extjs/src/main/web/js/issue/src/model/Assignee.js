Ext.define('Isu.model.Assignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            displayValue: 'ID',
            type: 'int'
        },
        {
            name: 'idx',
            displayValue: 'IDX',
            type: 'string',
            convert: function (value, record) {
                var idx = null,
                    id = record.get('id'),
                    type = record.get('type');

                if (id && type) {
                    idx = id + ':' + type;
                }

                return idx;
            }
        },
        {
            name: 'type',
            displayValue: 'Type',
            type: 'auto'
        },
        {
            name: 'name',
            displayValue: 'Name',
            type: 'auto'
        }
    ],

    idProperty: 'idx',

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees',
        reader: {
            type: 'json',
            root: 'data'
        },
        buildUrl: function(request) {
            var idx = request.params.id,
                params;

            if (idx) {
                params = idx.split(':');
                return this.url + '/' + params[0] + '?assigneeType=' + params[1];
            } else {
                return this.url
            }
        }
    }
});
