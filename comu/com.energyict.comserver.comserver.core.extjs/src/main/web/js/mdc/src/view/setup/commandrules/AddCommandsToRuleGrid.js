Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddCommandsToRuleGrid',

    requires: [
        'Uni.grid.column.ReadingType',
        'Dxp.view.tasks.SelectedReadingTypesWindow'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfCommandsSelected', count, 'MDC',
            'No commands selected', '{0} command selected', '{0} commands selected'
        );
    },

    bottomToolbarHidden: true,
    checkAllButtonPresent: true,

    columns: [
        {
            header: Uni.I18n.translate('general.category', 'MDC', 'Category'),
            dataIndex: 'category',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.command', 'MDC', 'Command'),
            dataIndex: 'command',
            flex: 1
        }
    ],

    onSelectionChange: function () {
        this.up('AddCommandsToRuleView').down('#mdc-command-rule-add-commands-addButton').setDisabled(this.getSelectedItems().length === 0);
        this.callParent(arguments);
    }

});