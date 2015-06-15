Ext.define('Isu.view.component.AssigneeColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-assignee-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result;

        if (!Ext.isEmpty(value)) {
            result = '';
            if (value.type) {
                result += '<span class="isu-icon-' + value.type + ' isu-assignee-type-icon" data-qtip="' + Uni.I18n.translate('assignee.tooltip.' + value.type, 'ISU', value.type) + '"></span> ';
            }
            if (value.name) {
                result += Ext.String.htmlEncode(value.name);
            }
        } else {
            result = ' '
        }

        return result || this.columns[colIndex].emptyText;
    }
});