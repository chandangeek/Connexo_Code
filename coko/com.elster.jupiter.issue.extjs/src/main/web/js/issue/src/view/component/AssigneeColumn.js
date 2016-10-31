Ext.define('Isu.view.component.AssigneeColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-assignee-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result

        if (value && value.hasOwnProperty('id')){
            var result = '';

            result += '<span class="isu-icon-USER isu-assignee-type-icon" data-qtip="';
            result += Uni.I18n.translate('assignee.tooltip.USER', 'ISU', 'User');
            result += '"></span>';

            if (value.name) {
                result += Ext.String.htmlEncode(value.name);
            }
        } else {
            result = ' ';
        }

        return result || this.columns[colIndex].emptyText;
    }
});