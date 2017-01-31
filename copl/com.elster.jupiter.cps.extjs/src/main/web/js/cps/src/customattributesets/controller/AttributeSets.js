/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cps.customattributesets.controller.AttributeSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Cps.customattributesets.view.Setup',
        'Cps.customattributesets.view.AttributesGrid',
        'Cps.customattributesets.view.EditLevelsWindow'
    ],

    requires: [
        'Cps.customattributesets.service.AttributeTransformer'
    ],

    stores: [
        'Cps.customattributesets.store.CustomAttributeSets',
        'Cps.customattributesets.store.Attributes',
        'Cps.customattributesets.store.AttributeTypes'
    ],


    refs: [
        {
            ref: 'attributesGrid',
            selector: 'custom-attribute-sets-setup #administration-custom-attributes-grid-id'
        },
        {
            ref: 'attributesGridPanel',
            selector: 'custom-attribute-sets-setup #administration-custom-attributes-grid-title-panel-id'
        },
        {
            ref: 'page',
            selector: 'custom-attribute-sets-setup'
        },
        {
            ref: 'attributeSetsGrid',
            selector: 'custom-attribute-sets-setup custom-attribute-sets-grid'
        }
    ],

    init: function () {
        this.control({
            'custom-attribute-sets-setup custom-attribute-sets-grid': {
                select: this.showAttributes
            },
            'custom-attribute-sets-action-menu #custom-attribute-sets-edit-levels': {
                click: this.editLevels
            },
            'custom-attribute-set-edit-levels': {
                saverecord: this.saveRecord
            }
        });
    },

    saveRecord: function (record) {
        var me = this,
            setupPage = me.getPage(),
            attributeSetsGrid = me.getAttributeSetsGrid();

        setupPage.setLoading(true);

        record.save({
            isNotEdit: true,
            success: function () {
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customattributesets.editlevels.acknowledge', 'CPS', 'New levels saved'));
                attributeSetsGrid.getStore().load();
            },
            callback: function () {
                setupPage.setLoading(false);
            }
        });
    },

    editLevels: function (button) {
        var record = this.getRecordFromBtn(button),
            widget = Ext.widget('custom-attribute-set-edit-levels', {record: record});

        widget.setTitle('');
        widget.show();
    },

    getRecordFromBtn: function (button) {
        var actionMenu = button.up('custom-attribute-sets-action-menu');

        return actionMenu ? actionMenu.record : null;
    },

    showAttributes: function (selectionModel, record) {
        var me = this,
            attributesGrid = me.getAttributesGrid(),
            attributesGridPanel = me.getAttributesGridPanel(),
            attributesStore = attributesGrid.getStore();

        Ext.suspendLayouts();
        me.setupMenuItems(record);
        attributesGridPanel.setTitle(Uni.I18n.translate('customattributesets.attributesof', 'CPS', 'Attributes of \'{0}\'', [Ext.String.htmlEncode(record.get('name'))]));
        attributesStore.removeAll();
        attributesStore.add(Cps.customattributesets.service.AttributeTransformer.transform(record.get('properties')));
        Ext.resumeLayouts();
    },

    setupMenuItems: function (record) {
        var menuItems = Ext.ComponentQuery.query('custom-attribute-sets-action-menu #custom-attribute-sets-edit-levels');
        if (!Ext.isEmpty(menuItems)) {
            Ext.Array.each(menuItems, function (item) {
                item.setVisible(record.get('domainNameUntranslated') !== 'com.elster.jupiter.servicecall.ServiceCall')
            });
        }
    },

    showCustomAttributeSets: function () {
        var me = this,
            widget;

        widget = Ext.widget('custom-attribute-sets-setup');
        me.getApplication().fireEvent('changecontentevent', widget);
    }
});

