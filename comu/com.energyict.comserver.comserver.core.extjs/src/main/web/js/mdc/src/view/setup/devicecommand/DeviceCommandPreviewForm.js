Ext.define('Mdc.view.setup.devicecommand.DeviceCommandPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceCommandPreviewForm',
    layout: {
        type: 'column'
    },
    items: [
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdName', 'MDC', 'Command name'),
                    name: 'messageSpecification',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val.name) : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdCategory', 'MDC', 'Command category'),
                    name: 'category',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                    name: 'status',
                    renderer: function (val) {
                        return val.displayValue ? Ext.String.htmlEncode(val.displayValue): ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdTrackingId', 'MDC', 'Tracking ID'),
                    name: 'trackingId',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdErrorMsg', 'MDC', 'Error message'),
                    name: 'errorMessage',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : ''
                    }
                }
            ]
        },
        {
            columnWidth: 0.5,
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdCreatedBy', 'MDC', 'Created By'),
                    name: 'user',
                    renderer: function (val) {
                        return val ? Ext.String.htmlEncode(val) : ''
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.cmdCreationDate', 'MDC', 'Creation date'),
                    name: 'creationDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.releaseDate', 'MDC', 'Release date'),
                    name: 'releaseDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '';
                    }
                },
                {
                    fieldLabel: Uni.I18n.translate('deviceCommands.view.sentDate', 'MDC', 'Sent date'),
                    name: 'sentDate',
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '';
                    }
                }
            ]
        }
    ]
});

