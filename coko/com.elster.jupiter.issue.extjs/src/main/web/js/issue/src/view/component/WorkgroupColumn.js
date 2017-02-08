/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.component.WorkgroupColumn', {
    extend: 'Ext.grid.column.Column',
    xtype: 'isu-workgroup-column',
    header: '',
    emptyText: '',

    renderer: function (value, metaData, record, rowIndex, colIndex) {
        var result;

        if (!Ext.isEmpty(value)&& value.hasOwnProperty('id')) {
            result = '';

            result += '<span class="isu-icon-GROUP isu-assignee-type-icon" data-qtip="';
            result += Uni.I18n.translate('assignee.tooltip.workgroup', 'ISU', 'Workgroup');
            result += '"></span>';

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