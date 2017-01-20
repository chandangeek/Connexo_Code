Ext.define('Isu.view.component.AssigneeColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-assignee-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result

        if (value && value.hasOwnProperty('id')){
            var result = '';

            if (value.name) {
                result += Ext.String.htmlEncode(value.name);
            }
        } else {
            result = '-';
        }

        return result || this.columns[colIndex].emptyText;
    }
});