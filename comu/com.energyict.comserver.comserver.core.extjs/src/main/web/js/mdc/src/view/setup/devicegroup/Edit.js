Ext.define('Mdc.view.setup.devicegroup.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-group-edit',
    itemId: 'device-group-edit',
    requires: [
        'Mdc.view.setup.devicesearch.DevicesSideFilter',
        'Mdc.view.setup.devicesearch.SearchResults',
        'Uni.util.FormErrorMessage'
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
                        text: Uni.I18n.translate('devicegroup.missingname', 'MDC', 'Please enter a name for the device group.'),
                        width: 600
                    },
                    {
                        itemId: 'step1-adddevicegroup-name-errors',
                        xtype: 'uni-form-error-message',
                        hidden: true,
                        text: Uni.I18n.translate('devicegroup.duplicatename', 'MDC', 'A device group with this name already exists.'),
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
                        fieldLabel: Uni.I18n.translate('devicetype.name', 'MDC', 'Name'),
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
                        fieldLabel: Uni.I18n.translate('comserver.preview.type', 'MDC', 'Type'),
                        width: 500
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices')
                    },
                    {
                        xtype: 'filter-top-panel'
                    },
                    {
                        xtype: 'mdc-search-results'
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
        me.side = {
            ui: 'medium',
            items: [
                {
                    margin: '16 0 0 0',
                    width: 250,
                    xtype: 'mdc-search-results-side-filter'
                }
            ]
        };
        me.callParent(arguments);
        me.setEdit();
    }
});

