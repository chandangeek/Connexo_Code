/**
 * @class Uni.view.grid.BulkSelection
 *
 * The bulk selection component is used for when all or selected items for a specific
 * screen need to be added. A checkbox is used to select the models in the infinite
 * scrolling grid below the radio options. You can either select all items or only
 * a few specific ones from the grid and then press the add button.
 *
 * The 'allitemsadd' or 'selecteditemsadd' event is fired respectively for the all
 * and selected items adding.
 *
 * Example:
 *     {
 *         xtype: 'Uni.view.grid.BulkSelection',
 *
 *         store: 'Mdc.store.ValidationRuleSetsForDeviceConfig',
 *
 *         counterTextFn: function (count) {
 *             return Uni.I18n.translatePluralxxx(
 *                 'validation.noValidationRuleSetSelected',
 *                 count,
 *                 'UNI',
 *                 '{0} validation rule sets selected'
 *             );
 *         },
 *
 *         allLabel: Uni.I18n.translate('ruleset.allRuleSets', 'UNI', 'All validation rule sets'),
 *         allDescription: Uni.I18n.translate(
 *             'ruleset.selectAllRuleSets',
 *             'UNI',
 *             'Select all validation rule sets related to device configuration'
 *         ),
 *
 *         selectedLabel: Uni.I18n.translate('ruleset.selectedRuleSets', 'UNI', 'Selected validation rule sets'),
 *         selectedDescription: Uni.I18n.translate(
 *             'ruleset.selectRuleSets',
 *             'UNI',
 *             'Select validation rule sets in table'
 *         ),
 *
 *         columns: [
 *             {
 *                 header: Uni.I18n.translate('validation.ruleSetName', 'UNI', 'Validation rule set'),
 *                 dataIndex: 'name',
 *                 renderer: function (value, metaData, record) {
 *                     metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
 *                     return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
 *                 },
 *                 flex: 1
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.activeRules', 'UNI', 'Active rules'),
 *                 dataIndex: 'numberOfRules',
 *                 flex: 1,
 *                 renderer: function (value, b, record) {
 *                     var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
 *                     return numberOfActiveRules;
 *                 }
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.inactiveRules', 'UNI', 'Inactive rules'),
 *                 dataIndex: 'numberOfInactiveRules',
 *                 flex: 1
 *             },
 *             {
 *                 xtype: 'uni-actioncolumn',
 *                 items: 'Mdc.view.setup.validation.AddRuleSetActionMenu'
 *             }
 *         ],
 *
 *         // Other code...
 *     }
 */
Ext.define('Uni.view.grid.BulkSelection', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'bulk-selection-grid',

    maxHeight: 600,

    /**
     * @cfg allLabel
     *
     * Text to show for the all items label.
     */
    allLabel: Uni.I18n.translate('grid.BulkSelection.allLabel', 'UNI', 'All items'),

    /**
     * @cfg allDescription
     *
     * Description to show under the all items label.
     */
    allDescription: Uni.I18n.translate(
        'grid.BulkSelection.allDescription',
        'UNI',
        'Select all items'
    ),

    /**
     * @cfg selectedLabel
     *
     * Text to show for the selected items label.
     */
    selectedLabel: Uni.I18n.translate('grid.BulkSelection.selectedLabel', 'UNI', 'Selected items'),

    /**
     * @cfg selectedDescription
     *
     * Description to show under the selected items label.
     */
    selectedDescription: Uni.I18n.translate(
        'grid.BulkSelection.selectedDescription',
        'UNI',
        'Select items in table'
    ),

    /**
     * @cfg addText
     *
     * Text used for the add button.
     */
    addText: Uni.I18n.translate('general.add', 'UNI', 'Add'),

    /**
     * @cfg cancelText
     *
     * Text used for the cancel button.
     */
    cancelText: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),

    /**
     * @cfg cancelHref
     *
     * The URL to be used for the cancel button.
     */
    cancelHref: window.location.href,

    /**
     * @cfg allChosenByDefault
     *
     * The property that determines what radio button will be selected when using the
     * bulk selection component. By default the all items option is chosen. Set this
     * to 'false' to select the selected items option.
     */
    allChosenByDefault: true,

    /**
     * @cfg allInputValue
     */
    allInputValue: 'allItems',

    /**
     * @cfg selectedInputValue
     */
    selectedInputValue: 'selectedItems',

    /**
     * @cfg radioGroupName
     */
    radioGroupName: 'selectedGroupType-' + new Date().getTime() * Math.random(),

    /**
     * @cfg bottomToolbarHidden
     */
    bottomToolbarHidden: false,

    gridHeight: 0,
    gridHeaderHeight: 0,

    initComponent: function () {
        var me = this;

        me.addEvents(
            /**
             * @event allitemsadd
             *
             * Fires after pressing the add button while having the all items option chosen.
             */
            'allitemsadd',
            /**
             * @event selecteditemsadd
             *
             * Fires after pressing the add button while having the selected items option chosen.
             *
             * @param {Ext.data.Model[]} The selected items.
             */
            'selecteditemsadd'
        );

        me.callParent(arguments);

        me.addDocked({
            xtype: 'radiogroup',
            dock: 'top',
            itemId: 'itemradiogroup',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 16 0'
            },
            items: [
                {
                    name: me.radioGroupName,
                    boxLabel: '<b>' + me.allLabel + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + me.allDescription + '</span>',
                    inputValue: me.allInputValue,
                    checked: me.allChosenByDefault
                },
                {
                    name: me.radioGroupName,
                    boxLabel: '<b>' + me.selectedLabel + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + me.selectedDescription + '</span>',
                    inputValue: me.selectedInputValue,
                    checked: !me.allChosenByDefault
                }
            ]
        }, 0);

        me.addDocked({
            xtype: 'toolbar',
            dock: 'bottom',
            itemId: 'bottomToolbar',
            layout: 'hbox',
            hidden: me.bottomToolbarHidden,
            items: [
                {
                    xtype: 'button',
                    itemId: 'addButton',
                    text: me.addText,
                    action: 'add',
                    ui: 'action'
                },
                {
                    xtype: 'button',
                    itemId: 'cancelButton',
                    text: me.cancelText,
                    action: 'cancel',
                    ui: 'link',
                    href: me.cancelHref
                }
            ]
        });

        me.getSelectionGroupType().on('change', me.onChangeSelectionGroupType, me);
        me.getAddButton().on('click', me.onClickAddButton, me);
        me.on('selectionchange', me.onBulkSelectionChange, me);

        me.store.on('load', me.onSelectDefaultGroupType, me, {
            single: true
        });
    },

    onSelectDefaultGroupType: function () {
        var me = this;

        if (me.rendered) {
            me.onChangeSelectionGroupType();
        }
    },

    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this;
        if (me.view) {
            var selection = me.view.getSelectionModel().getSelection();

            Ext.suspendLayouts();
            me.getAddButton().setDisabled(!me.isAllSelected() && selection.length === 0);
            me.setGridVisible(!me.isAllSelected());
            Ext.resumeLayouts(true);
        }
    },

    setGridVisible: function (visible) {
        var me = this,
            gridHeight = me.gridHeight,
            gridHeaderHeight = me.gridHeaderHeight,
            currentGridHeight,
            currentGridHeaderHeight,
            noBorderCls = 'force-no-border';

        me.getTopToolbarContainer().setVisible(visible);

        if (me.rendered) {
            currentGridHeight = me.getView().height;
            currentGridHeaderHeight = me.headerCt.height;

            if (!visible) {
                gridHeight = 0;
                gridHeaderHeight = 0;

                me.addCls(noBorderCls);
            } else {
                me.removeCls(noBorderCls);
            }

            if (currentGridHeight !== 0 && currentGridHeaderHeight !== 0) {
                me.gridHeight = currentGridHeight;
                me.gridHeaderHeight = currentGridHeaderHeight;
            }

            if (typeof gridHeight === 'undefined') {
                var row = me.getView().getNode(0),
                    rowElement = Ext.get(row),
                    count = me.store.getCount() > 10 ? 10 : me.store.getCount();

                if (rowElement !== null) {
                    gridHeight = count * rowElement.getHeight();
                } else {
                    gridHeight = count * 29;
                }
            }

            me.getView().height = gridHeight;
            me.headerCt.height = gridHeaderHeight;
            me.doLayout();
        }
    },

    onClickAddButton: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        if (me.isAllSelected()) {
            me.fireEvent('allitemsadd');
        } else if (selection.length > 0) {
            me.fireEvent('selecteditemsadd', selection);
        }
    },

    onBulkSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getAddButton().setDisabled(!me.isAllSelected() && selection.length === 0);
    },

    isAllSelected: function () {
        var me = this,
            groupType = me.getSelectionGroupType().getValue();

        return groupType[me.radioGroupName] === me.allInputValue;
    },

    getSelectionGroupType: function () {
        return this.down('radiogroup');
    },

    getAddButton: function () {
        return this.down('#addButton');
    },

    getCancelButton: function () {
        return this.down('#cancelButton');
    },

    getBottomToolbar: function () {
        return this.down('#bottomToolbar');
    },

    hideBottomToolbar: function () {
        this.getBottomToolbar().setVisible(false);
    }
});