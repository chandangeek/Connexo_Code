Ext.define('Isu.model.IssueAssignee', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'int'
        },
        /*  {
            name: 'idx',
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
         },*/
        /*  {
            name: 'type',
            type: 'auto'
         },*/
        {
            name: 'name',
            type: 'auto'
        }
    ],

    //idProperty: 'idx',

    // GET ?like="operator"
    proxy: {
        type: 'rest',
        url: '/api/isu/assignees',
        reader: {
            type: 'json',
            root: 'data'
        },
        buildUrl: function(request) {
            return this.url;
            var idx = request.params.id,
                params;

            if (idx) {
                params = idx.split(':');
                return this.url + '/' + params[0];// + '?assigneeType=' + params[1];
            } else {
                return this.url
            }
        }
    }
});