Ext.define('Mdc.customattributesets.controller.AttributeSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.customattributesets.view.Setup',
        'Mdc.customattributesets.view.AttributesGrid',
        'Mdc.customattributesets.view.EditLevelsWindow'
    ],

    requires: [
        'Mdc.customattributesets.service.AttributeTransformer'
    ],

    stores: [
        'Mdc.customattributesets.store.CustomAttributeSets',
        'Mdc.customattributesets.store.Attributes',
        'Mdc.customattributesets.store.AttributeTypes'
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

    saveRecord: function(record) {
        var me = this,
            setupPage = me.getPage(),
            attributeSetsGrid = me.getAttributeSetsGrid();

        setupPage.setLoading(true);

        record.save({
            callback: function () {
                setupPage.setLoading(false);
                attributeSetsGrid.getStore().load();
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

        attributesGridPanel.setTitle(Uni.I18n.translate('customattributesets.attributesof', 'MDC', 'Attributes of \'{0}\'', [Ext.String.htmlEncode(record.get('name'))]));
        attributesStore.removeAll();
        attributesStore.add(Mdc.customattributesets.service.AttributeTransformer.transform(record.get('attributes')));
    },

    showCustomAttributeSets: function () {
        var me = this,
            widget;

        widget = Ext.widget('custom-attribute-sets-setup');
        me.getApplication().fireEvent('changecontentevent', widget);
    }
});

