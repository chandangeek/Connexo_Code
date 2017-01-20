Ext.define('Isu.view.component.WorkgroupColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-workgroup-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result;

        if (!Ext.isEmpty(value)&& value.hasOwnProperty('id')) {
            result = '';

            if (value.name) {
                result += Ext.String.htmlEncode(value.name);
            }
            //metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(value.name) + '"';
        } else {
            result = '-'
        }
        return result || this.columns[colIndex].emptyText;
    }
});