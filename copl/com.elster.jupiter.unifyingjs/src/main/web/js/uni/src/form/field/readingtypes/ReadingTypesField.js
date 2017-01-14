Ext.define('Uni.form.field.readingtypes.ReadingTypesField', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.reading-types-field',
    requires: [
        'Uni.model.ReadingType',
        'Uni.form.field.readingtypes.AddingView',
        'Uni.form.field.readingtypes.Controller',
        'Uni.util.History',
        'Uni.model.ReadingType'
    ],
    mixins: {
        field: 'Ext.form.field.Field'
    },
    layout: 'hbox',
    msgTarget: 'under',
    fieldLabel: Uni.I18n.translate('readingTypesField.readingTypes', 'UNI', 'Reading types'),

    isEquidistant: false,
    isActive: false,
    additionalReasons: null,

    initComponent: function () {
        var me = this;

        me.readingTypesStore = Ext.create('Ext.data.Store', {
            model: 'Uni.model.ReadingType'
        });

        me.minWidth = me.labelWidth + 650;

        me.items = [
            {
                xtype: 'displayfield',
                itemId: 'reading-types-empty-text',
                htmlEncode: false,
                style: 'font-style: italic',
                value: '<span style="color: #686868; font-style: italic">'
                + Uni.I18n.translate('readingTypesField.noAdded', 'UNI', 'No reading types have been added')
                + '</span>'
            },
            {
                xtype: 'grid',
                itemId: 'added-reading-types-grid',
                store: me.readingTypesStore,
                width: 500,
                padding: 0,
                maxHeight: 323,
                hidden: true,
                columns: [
                    {
                        xtype: 'reading-type-column',
                        valueIsRecordData: true,
                        flex: 1
                    },
                    {
                        xtype: 'uni-actioncolumn-remove',
                        isDisabled: function () {
                            return me.disabled
                        },
                        handler: function (grid, rowIndex) {
                            grid.getStore().removeAt(rowIndex);
                            me.updateView();
                        }
                    }
                ]
            },
            {
                xtype: 'button',
                itemId: 'add-reading-types-button',
                text: Uni.I18n.translate('readingTypesField.addReadingTypes', 'UNI', 'Add reading types'),
                action: 'addReadingTypes',
                margin: '0 0 0 10',
                handler: Ext.bind(me.showAddView, me)
            }
        ];

        me.callParent(arguments);
    },

    updateView: function () {
        var me = this,
            grid = me.down('#added-reading-types-grid'),
            emptyText = me.down('#reading-types-empty-text'),
            hasReadingTypes = !!grid.getStore().getCount();

        Ext.suspendLayouts();
        grid.getStore().sort('fullAliasName', 'ASC');
        grid.setVisible(hasReadingTypes);
        emptyText.setVisible(!hasReadingTypes);
        Ext.resumeLayouts(true);
    },

    setValue: function (value) {
        var me = this;

        if (Ext.isArray(value) && value.length) {
            me.down('#added-reading-types-grid').getStore().loadData([], false);
            me.down('#added-reading-types-grid').getStore().add(value);
        }
        me.updateView();

        me.value = value;
    },

    getValue: function () {
        var me = this,
            value = null;

        if (me.readingTypesStore.getCount()) {
            value = _.map(me.readingTypesStore.getRange(), function (readingType) {
                return readingType.getData();
            });
        }

        return value;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError();

        Ext.suspendLayouts();
        me.items.each(function (item) {
            if (item.isFormField) {
                if (error) {
                    item.addCls('x-form-invalid');
                } else {
                    item.removeCls('x-form-invalid');
                }
            }
        });
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    },

    showAddView: function () {
        var me = this,
            contentPanel = Ext.ComponentQuery.query('viewport > #contentPanel')[0];

        me.currentPageView = contentPanel.down();

        me.addReadingTypesView = Ext.widget('uni-add-reading-types-view', {
            itemId: 'uni-add-reading-types-view',
            selectedReadingTypes: me.getValue(),
            isEquidistant: me.isEquidistant,
            isActive: me.isActive,
            additionalReasons: me.additionalReasons
        });

        me.addReadingTypesView.on('addReadingTypes', function (readingTypes) {
            me.hideAddView();
            me.setValue(readingTypes);
        }, me, {single: true});

        Ext.suspendLayouts();
        me.currentPageView.hide();
        contentPanel.add(me.addReadingTypesView);
        me.setPseudoNavigation(true);
        Ext.resumeLayouts(true);
    },

    hideAddView: function () {
        var me = this;

        Ext.suspendLayouts();
        me.setPseudoNavigation();
        me.addReadingTypesView.destroy();
        me.currentPageView.show();
        Ext.resumeLayouts(true);
    },

    setPseudoNavigation: function (toAddReadingTypes) {
        var me = this,
            breadcrumbTrail = Ext.ComponentQuery.query('breadcrumbTrail')[0],
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];

        if (toAddReadingTypes) {
            lastBreadcrumbLink.renderData.href = window.location.href;
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
            breadcrumbTrail.addBreadcrumbItem(Ext.create('Uni.model.BreadcrumbItem', {
                text: Uni.I18n.translate('readingTypesField.addReadingTypes', 'UNI', 'Add reading types'),
                relative: false
            }));
            lastBreadcrumbLink.getEl().on('click', me.hideAddView, me, {single: true});
        } else {
            lastBreadcrumbLink.destroy();
            breadcrumbTrail.query('breadcrumbSeparator:last')[0].destroy();
            lastBreadcrumbLink = breadcrumbTrail.query('breadcrumbLink:last')[0];
            lastBreadcrumbLink.renderData.href = '';
            lastBreadcrumbLink.update(lastBreadcrumbLink.renderTpl.apply(lastBreadcrumbLink.renderData));
        }
    }
});