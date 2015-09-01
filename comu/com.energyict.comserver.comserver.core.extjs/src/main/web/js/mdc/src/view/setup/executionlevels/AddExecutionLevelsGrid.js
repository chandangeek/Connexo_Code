Ext.define('Mdc.view.setup.executionlevels.AddExecutionLevelsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-execution-levels-grid',
    store: 'AvailableExecLevelsForSecSettingsOfDevConfig',
    height: 300,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('executionlevels.selectedItems', count, 'MDC',
            'No privileges selected', '{0} privilege selected', '{0} privileges selected');
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('executionLevel.executionlevel', 'MDC', 'Privilege'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('executionLevel.userroles', 'MDC', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(Ext.String.htmlEncode(userRole.name));
                    });
                    return resultArray.join('<br>');
                },
                flex: 1
            }
        ]
    }
});
