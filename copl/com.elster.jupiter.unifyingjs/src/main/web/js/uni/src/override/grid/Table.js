Ext.define('Uni.override.grid.Table', {
    override: 'Ext.view.Table',

    getRecord: function(node) {
        node = this.getNode(node);
        if (node) {
            var recordId = node.getAttribute('data-recordid');
            if (recordId) {

                // The Grouping Feature increments the index to skip over unrendered records in collapsed groups
                var record = this.store.data.map[recordId];
                if(record)
                {
                    return record;
                }

            }
            return this.dataSource.data.get(node.getAttribute('data-recordId'));
        }
    }

});


