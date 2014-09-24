Ext.define('Mdc.view.setup.executionlevels.AddExecutionLevelsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-execution-levels-grid',
    store: 'AvailableExecLevelsForSecSettingsOfDevConfig',
    height: 300,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'executionlevels.selectedItems',
            count,
            'MDC',
            '{0} execution levels selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('executionLevel.executionlevel', 'MDC', 'Execution level'),
                dataIndex: 'name',
                flex: 0.3
            },
            {
                header: Uni.I18n.translate('executionLevel.userroles', 'MDC', 'User roles'),
                dataIndex: 'userRoles',
                renderer: function (value) {
                    var resultArray = [];
                    Ext.Array.each(value, function (userRole) {
                        resultArray.push(userRole.name);
                    });
                    return resultArray.join('<br>');
                },
                flex: 0.3
            }
        ]
    }
});
