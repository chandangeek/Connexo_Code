/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.form.field.ExecutionLevelDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'execution-level-displayfield',
    emptyText: '-',
    tooltip: '',

    requires: [
        'Uni.view.window.ExecutionLevelDetails'
    ],

    renderer: function (value, field) {
        if (Ext.isEmpty(value)) {
            return this.emptyText;
        }
        var result = '';
        if (Ext.isArray(value.levels)) {
            Ext.Array.each(value.levels, function(level) {
                result += level.name + ' - ';
            });
            result = result.slice(0, result.length - 3);
        }
        var icon = '<span class="icon-info" style="margin-left: 10px; cursor:pointer; display:inline-block; font-size:16px; line-height: 13px; vertical-align: middle;' +
                    '" data-qtip="' + Uni.I18n.translate('readingType.tooltip', 'UNI', 'Click for more information') + '"></span>';

        setTimeout(function () {
            var parent,
                iconEl;

            parent = field.getEl();
            if (Ext.isDefined(parent)) {
                iconEl = parent.down('.icon-info');
            }
            if (Ext.isDefined(iconEl)) {
                iconEl.clearListeners();
                iconEl.on('click', function () {
                    field.handler(value.defaultLevels);
                });
            }
        }, 1);

        return result + icon;
    },

    handler: function (defaultLevelsArray) {
        var widget = Ext.widget('execution-level-details');
        widget.setDefaultLevels(defaultLevelsArray);
        widget.show();
    }

});