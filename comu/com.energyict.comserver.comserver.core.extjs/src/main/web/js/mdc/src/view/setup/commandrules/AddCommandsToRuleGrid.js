Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddCommandsToRuleGrid',

    requires: [
        'Mdc.store.SelectedCommands',
        'Mdc.view.setup.commandrules.SelectedCommandsWindow'
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
    hiddenSelection: [],

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

    initComponent: function () {
        var me = this;
        me.hiddenSelection = [];
        me.callParent(arguments);

        me.on('select', function (grid, record) {
            me.addToSelection(record);
        });
        me.on('deselect', function (grid, record) {
            me.removeFromSelection(record);
        });

        me.getTopToolbarContainer().add(1,{
            xtype: 'button',
            hidden: true,
            itemId: 'mdc-add-commands-to-rule-selectedCommands-info-btn',
            tooltip: Uni.I18n.translate('general.moreInfo.tooltip', 'MDC', 'Click for more information'),
            iconCls: 'uni-icon-info-small',
            cls: 'uni-btn-transparent',
            width: 15,
            style: {
                display: 'inline-block',
                textDecoration: 'none !important',
                position: 'absolute',
                top: '5px'
            },
            handler: function () {
                var widget = Ext.widget('AddCommandsToRuleSelectedCommands');
                widget.setTitle(me.counterTextFn(me.hiddenSelection.length));
                widget.show();
            }
        });
    },

    addToSelection: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('commandName') === record.get('commandName')) {
                index = ind;
            }
        });
        if (index < 0) {
            this.hiddenSelection.push(record);
        }
    },

    removeFromSelection: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('commandName') === record.get('commandName')) {
                index = ind;
            }
        });
        if (index > -1) {
            this.hiddenSelection.splice(index, 1);
        }
    },

    getSelectedItems: function() {
        return this.hiddenSelection
    },

    clearSelection: function() {
        this.hiddenSelection = [];
    },

    onSelectionChange: function () {
        var me = this,
            selectionInfoBtn = me.down('#mdc-add-commands-to-rule-selectedCommands-info-btn'),
            selectedStore = Ext.getStore('Mdc.store.SelectedCommands');

        selectedStore.removeAll();
        selectedStore.add(me.hiddenSelection);
        selectionInfoBtn.setVisible(me.hiddenSelection.length>0);
        me.up('AddCommandsToRuleView').down('#mdc-command-rule-add-commands-addButton').setDisabled(this.getSelectedItems().length === 0);
        me.callParent(arguments);
    },

    onClickUncheckAllButton: function (button) {
        var me = this;
        me.callParent(arguments);
        me.clearSelection();
        me.onSelectionChange();
    }

});