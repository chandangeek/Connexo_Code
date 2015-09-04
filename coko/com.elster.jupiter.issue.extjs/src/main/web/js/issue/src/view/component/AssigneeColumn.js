Ext.define('Isu.view.component.AssigneeColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-assignee-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result,
            tooltip;

        if (!Ext.isEmpty(value)) {
            result = '';
            if (value.type) {
                switch(value.type){
                    case 'USER':
                        tooltip = Uni.I18n.translate('assignee.tooltip.USER', 'ISU', 'User');
                        break;
                    case 'GROUP':
                        tooltip = Uni.I18n.translate('assignee.tooltip.GROUP', 'ISU', 'User group');
                        break;
                    case 'ROLE':
                        tooltip = Uni.I18n.translate('assignee.tooltip.ROLE', 'ISU', 'User role');
                        break;
                }
                result += '<span class="isu-icon-' + value.type + ' isu-assignee-type-icon" data-qtip="' + tooltip + '"></span> ';
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