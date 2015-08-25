Ext.define('Mdc.view.setup.devicegroup.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-group-edit',
    itemId: 'device-group-edit',
    requires: [
        'Mdc.view.setup.devicesearch.DevicesTopFilter',
        'Mdc.view.setup.devicesearch.BufferedDevicesTopFilter',
        'Mdc.store.filter.DeviceTypes',
        'Mdc.store.DeviceConfigurations',
        'Mdc.view.setup.devicesearch.SearchResults',
        'Uni.util.FormErrorMessage',
        'Mdc.store.Devices',
        'Mdc.store.DevicesBuffered'
    ],
    returnLink: null,
    setEdit: function () {
        if (this.returnLink) {
            this.down('#edit-device-group-cancel-link').href = this.returnLink;
        }
    },
    initComponent: function () {
        var me = this;
        me.content = [
            {
                itemId: 'device-group-edit-panel',
                ui: 'large',
                items: [
                    {
                        itemId: 'step1-adddevicegroup-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        width: 600
                    },
                    {
                        itemId: 'step2-adddevicegroup-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        text: Uni.I18n.translate('devicegroup.noDevicesSelected', 'MDC', 'Please select at least one device.'),
                        width: 600
                    },
                    {
                        xtype: 'textfield',
                        itemId: 'deviceGroupNameTextField',
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        allowBlank: false,
                        enforceMaxLength: true,
                        maxLength: 80,
                        width: 500
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'device-group-type',
                        required: true,
                        disabled: true,
                        fieldLabel: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                        width: 500
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('general.devices', 'MDC', 'Devices')
                    },
                    {
                        xtype: 'mdc-search-results'
                    }
                ],
                dockedItems: [
                    {
                        xtype: 'mdc-view-setup-devicesearch-devicestopfilter',
                        dock: 'top',
                        hidden: true,
                        margin: '0 0 10 0'
                    },
                    {
                        xtype: 'mdc-view-setup-devicesearch-buffereddevicestopfilter',
                        dock: 'top',
                        hidden: true,
                        margin: '0 0 10 0'
                    }
                ],
                buttons: [
                    {
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                        ui: 'action',
                        itemId: 'edit-device-group-action'
                    },
                    {
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                        itemId: 'edit-device-group-cancel-link',
                        ui: 'link',
                        href: '#/devices/devicegroups'
                    },
                    '->'
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit();
    },

    showDynamicFilter: function() {
        var me = this,
            rendered = me.rendered,
            dynamicFilter = me.down('mdc-view-setup-devicesearch-devicestopfilter'),
            staticFilter = me.down('mdc-view-setup-devicesearch-buffereddevicestopfilter');

        if (rendered) {
            dynamicFilter.setVisible(true);
            staticFilter.setVisible(false);
        } else {
            dynamicFilter.hidden = false;
            staticFilter.hidden = true;
        }
    },

    showStaticFilter: function() {
        var me = this,
            rendered = me.rendered,
            dynamicFilter = me.down('mdc-view-setup-devicesearch-devicestopfilter'),
            staticFilter = me.down('mdc-view-setup-devicesearch-buffereddevicestopfilter');

        if (rendered) {
            staticFilter.setVisible(true);
            dynamicFilter.setVisible(false);
        } else {
            dynamicFilter.hidden = true;
            staticFilter.hidden = false;
        }
    },

    setDynamicFilter: function(queryObject) {
        var filter = this.down('mdc-view-setup-devicesearch-devicestopfilter');
        if (queryObject.hasOwnProperty('deviceTypes') && Ext.isArray(queryObject.deviceTypes) && queryObject.deviceTypes.length === 1) {
            // in case of exactly one device type chosen, make sure the device configuration combo is also visible:
            filter.applyQueryObject(queryObject, false);
            filter.onDeviceTypeChanged();
        }
        filter.applyQueryObject(queryObject, true);
    }

});

