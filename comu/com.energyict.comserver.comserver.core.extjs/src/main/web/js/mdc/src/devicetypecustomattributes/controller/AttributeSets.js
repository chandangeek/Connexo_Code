Ext.define('Mdc.devicetypecustomattributes.controller.AttributeSets', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.devicetypecustomattributes.view.Setup',
        'Mdc.devicetypecustomattributes.view.AddAttributeSetsSetup'
    ],

    requires: [
        'Mdc.model.DeviceType'
    ],

    stores: [
        'Mdc.devicetypecustomattributes.store.CustomAttributeSets',
        'Mdc.devicetypecustomattributes.store.CustomAttributeSetsAvailable'
    ],

    refs: [
        {
            ref: 'addCustomattributeSetsButton',
            selector: '#device-type-add-custom-attribute-sets-setup-id #add-custom-attribute-sets-grid-add'
        },
        {
            ref: 'customAttributeSetsSelectionGrid',
            selector: '#device-type-add-custom-attribute-sets-setup-id #device-type-add-custom-attribute-sets-grid-id'
        },
        {
            ref: 'customAttributeSetsSelectionSetup',
            selector: '#device-type-add-custom-attribute-sets-setup-id'
        },
        {
            ref: 'customAttributeSetsSetup',
            selector: '#device-type-custom-attribute-sets-setup-id'
        },
        {
            ref: 'customAttributeSetsGrid',
            selector: '#device-type-custom-attribute-sets-setup-id #device-type-custom-attribute-sets-grid-id'
        }
    ],

    init: function () {
        this.control({
            '#device-type-custom-attribute-sets-setup-id button[action=addAttributeSets]': {
                click: this.moveToAddAttributeSets
            },
            '#device-type-add-custom-attribute-sets-setup-id #add-custom-attribute-sets-grid-cancel': {
                click: this.moveToCustomAttributes
            },
            '#device-type-add-custom-attribute-sets-setup-id #add-custom-attribute-sets-grid-add': {
                click: this.addSelectedSets
            },
            '#device-type-add-custom-attribute-sets-setup-id #device-type-add-custom-attribute-sets-grid-id': {
                selectionchange: this.manageAddButton
            },
            'device-type-custom-attribute-sets-action-menu #device-type-custom-attribute-sets-remove': {
                click: this.showRemoveConfirmationMessage
            }
        });
    },

    showRemoveConfirmationMessage: function (button) {
        var me = this,
            menu = button.up('device-type-custom-attribute-sets-action-menu');

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('customattributesets.remove.question', 'MDC', "Remove custom attribute set '{0}'?", encodeURIComponent(menu.record.get('name'))),
            msg: Uni.I18n.translate('customattributesets.remove.confirmation', 'MDC', 'You will no longer be able to use this custom attribute set on devices of this device type. Current device values will be lost.'),
            fn: function (state) {
                switch (state) {
                    case 'confirm':
                        me.removeCustomAttributeSet(menu.record);
                        break;
                }
            }
        });
    },

    removeCustomAttributeSet: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            setupPage = me.getCustomAttributeSetsSetup();

        setupPage.setLoading();
        record.getProxy().setUrl(router.arguments.deviceTypeId);
        record.destroy({
            success: function () {
                me.moveToCustomAttributes();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customattributesets.set.removed', 'MDC', 'Custom attribute set removed'));
            },
            callback: function () {
                setupPage.setLoading(false);
            }
        });
    },

    addSelectedSets: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            selectionGrid = me.getCustomAttributeSetsSelectionGrid(),
            selection = selectionGrid.getSelectionModel().getSelection(),
            setupPage = me.getCustomAttributeSetsSelectionSetup(),
            selectedItems = [],
            url;

        Ext.Array.each(selection, function (item) {
            selectedItems.push({id :item.get('id')});
        });

        url = '/api/dtc/devicetypes/{deviceTypeId}/custompropertysets'.replace('{deviceTypeId}', router.arguments.deviceTypeId);
        setupPage.setLoading();

        Ext.Ajax.request({
            url: url,
            method: 'PUT',
            jsonData: Ext.encode(selectedItems),
            success: function () {
                me.moveToCustomAttributes();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('customattributesets.added', 'MDC', 'Custom attribute sets added'));
            },
            callback: function () {
                setupPage.setLoading(false);
            }
        });
    },

    manageAddButton: function (grid, selection) {
        this.getAddCustomattributeSetsButton().setDisabled(selection.length === 0);
    },

    moveToCustomAttributes: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/customattributesets').forward();
    },

    moveToAddAttributeSets: function () {
        this.getController('Uni.controller.history.Router').getRoute('administration/devicetypes/view/customattributesets/add').forward();
    },

    showCustomAttributeSets: function (deviceTypeId) {
        var me = this,
            customAttributesSetsStore = me.getStore('Mdc.devicetypecustomattributes.store.CustomAttributeSets'),
            widget;

        customAttributesSetsStore.getProxy().setUrl(deviceTypeId);
        widget = Ext.widget('device-type-custom-attribute-sets-setup', {deviceTypeId: deviceTypeId});
        me.getApplication().fireEvent('changecontentevent', widget);
        me.loadDeviceTypeModel(me, widget, deviceTypeId);
    },

    showAddCustomAttributeSets: function (deviceTypeId) {
        var me = this,
            customAttributesSetsStore = me.getStore('Mdc.devicetypecustomattributes.store.CustomAttributeSetsAvailable'),
            widget;

        customAttributesSetsStore.getProxy().setUrl(deviceTypeId);
        widget = Ext.widget('device-type-add-custom-attribute-sets-setup', {deviceTypeId: deviceTypeId});
        me.getApplication().fireEvent('changecontentevent', widget);
        customAttributesSetsStore.load(function () {
            widget.down('device-type-add-custom-attribute-sets-grid').getSelectionModel().deselectAll();
        });
        me.loadDeviceTypeModel(me, widget, deviceTypeId);
    },

    loadDeviceTypeModel: function(scope, widget, deviceTypeId) {
        Ext.ModelManager.getModel('Mdc.model.DeviceType').load(deviceTypeId, {
            success: function (deviceType) {
                scope.getApplication().fireEvent('loadDeviceType', deviceType);
                if (widget.down('deviceTypeSideMenu')) {
                    widget.down('deviceTypeSideMenu').setDeviceTypeLink(deviceType.get('name'));
                    widget.down('deviceTypeSideMenu #conflictingMappingLink').setText(Uni.I18n.translate('deviceConflictingMappings.ConflictingMappingCount', 'MDC', 'Conflicting mappings ({0})', [deviceType.get('deviceConflictsCount')]));
                }
            }
        });
    }
});

