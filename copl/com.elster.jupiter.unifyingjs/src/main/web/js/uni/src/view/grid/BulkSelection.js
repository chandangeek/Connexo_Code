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
 *             return Uni.I18n.translatePlural(
 *                 'validation.noValidationRuleSetSelected',
 *                 count,
 *                 'MDC',
 *                 '{0} validation rule sets selected'
 *             );
 *         },
 *
 *         allLabel: Uni.I18n.translate('ruleset.allRuleSets', 'MDC', 'All validation rule sets'),
 *         allDescription: Uni.I18n.translate(
 *             'ruleset.selectAllRuleSets',
 *             'MDC',
 *             'Select all validation rule sets related to device configuration'
 *         ),
 *
 *         selectedLabel: Uni.I18n.translate('ruleset.selectedRuleSets', 'MDC', 'Selected validation rule sets'),
 *         selectedDescription: Uni.I18n.translate(
 *             'ruleset.selectRuleSets',
 *             'MDC',
 *             'Select validation rule sets in table'
 *         ),
 *
 *         columns: [
 *             {
 *                 header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
 *                 dataIndex: 'name',
 *                 renderer: function (value, metaData, record) {
 *                     metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
 *                     return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
 *                 },
 *                 flex: 1
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
 *                 dataIndex: 'numberOfRules',
 *                 flex: 1,
 *                 renderer: function (value, b, record) {
 *                     var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
 *                     return numberOfActiveRules;
 *                 }
 *             },
 *             {
 *                 header: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
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
    extend: 'Ext.grid.Panel',
    xtype: 'bulk-selection-grid',

    requires: [
        'Ext.grid.plugin.BufferedRenderer'
    ],

    bottomToolbarHeight: 27,

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        showHeaderCheckbox: false
    },

    maxHeight: 600,

    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 5,
            leadingBufferZone: 5,
            scrollToLoadBuffer: 10,
            onViewResize: function (view, width, height, oldWidth, oldHeight) {
                if (height === 0 || oldHeight === 0) {
                    var me = this,
                        count = view.all.getCount(),
                        newHeight = count * me.rowHeight;

                    if (count > 10) {
                        newHeight = 10 * me.rowHeight;
                    }

                    if (view.getHeight() !== 0 && view.getHeight() !== newHeight) {
                        view.setHeight(newHeight);
                    }
                }
            }
        }
    ],

    /**
     * @cfg counterTextFn
     *
     * The translation function to use to translate the selected count on top of the
     * text above the grid.
     *
     * @param {Number} count Count to base the translation on.
     * @returns {String} Translation value based on the count.
     */
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'grid.BulkSelection.counterText',
            count,
            'UNI',
            '{0} items selected'
        );
    },

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
     * @cfg uncheckText
     *
     * Text used for the uncheck all button.
     */
    uncheckText: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),

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

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'radiogroup',
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
                                boxLabel: '<b>' + me.allLabel + '</b><br/>' +
                                    '<span style="color: grey;">' + me.allDescription + '</span>',
                                inputValue: me.allInputValue,
                                checked: me.allChosenByDefault
                            },
                            {
                                name: me.radioGroupName,
                                boxLabel: '<b>' + me.selectedLabel + '</b><br/>' +
                                    '<span style="color: grey;">' + me.selectedDescription + '</span>',
                                inputValue: me.selectedInputValue,
                                checked: !me.allChosenByDefault
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        itemId: 'topToolbarContainer',
                        layout: {
                            type: 'hbox',
                            align: 'middle'
                        },
                        items: [
                            {
                                xtype: 'text',
                                itemId: 'selectionCounter',
                                text: me.counterTextFn(0),
                                margin: '0 8 0 0'
                            },
                            {
                                xtype: 'button',
                                itemId: 'uncheckAllButton',
                                text: me.uncheckText,
                                action: 'uncheckAll',
                                margin: '0 0 0 8',
                                disabled: true
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'toolbar',
                dock: 'bottom',
                itemId: 'bottomToolbar',
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
            }
        ];

        me.callParent(arguments);

        me.getSelectionGroupType().on('change', me.onChangeSelectionGroupType, me);
        me.getUncheckAllButton().on('click', me.onClickUncheckAllButton, me);
        me.getAddButton().on('click', me.onClickAddButton, me);
        me.on('selectionchange', me.onSelectionChange, me);

        if (me.radioHidden) {
            me.hideRadioGroup();
        }

        if (me.bottomToolbarHidden) {
            me.hideBottomToolbar();
        }

        me.on('afterrender', me.onChangeSelectionGroupType, me, {
            single: true
        });

        // Forces the view to update itself, the height value is not important.
        me.getView().setHeight(16);
    },

    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getAddButton().setDisabled(!me.isAllSelected() && selection.length === 0);
        me.setGridVisible(!me.isAllSelected());
    },

    setGridVisible: function (visible) {
        var me = this,
            gridHeight = me.gridHeight,
            gridHeaderHeight = me.gridHeaderHeight,
            currentGridHeight,
            currentGridHeaderHeight,
            noBorderCls = 'force-no-border';

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

            me.getTopToolbarContainer().setVisible(visible);
            me.getView().height = gridHeight;
            me.headerCt.height = gridHeaderHeight;
            me.doLayout();
        }
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
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

    onSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection(),
            value = {};

        value[me.radioGroupName] = me.selectedInputValue;
        me.getSelectionGroupType().setValue(value);

        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
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

    getSelectionCounter: function () {
        return this.down('#selectionCounter');
    },

    getUncheckAllButton: function () {
        return this.down('#uncheckAllButton');
    },

    getAddButton: function () {
        return this.down('#addButton');
    },

    getCancelButton: function () {
        return this.down('#cancelButton');
    },

    getTopToolbarContainer: function () {
        return this.down('#topToolbarContainer');
    },

    getBottomToolbar: function () {
        return this.down('#bottomToolbar');
    },

    hideRadioGroup: function () {
        this.down('#itemradiogroup').setVisible(false);
    },

    hideBottomToolbar: function () {
        this.getBottomToolbar().setVisible(false);
    }
});