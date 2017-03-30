/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.filemanagement.view.SpecificationsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.files-devicetype-specifications-form',
    layout: {
        type: 'form'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('timeofuse.fileManagementAllowed', 'MDC', 'File management allowed'),
                        name: 'fileManagementEnabled',
                        renderer: function (value) {
                            if (value) {
                                return Uni.I18n.translate('general.yes', 'MDC', 'Yes')
                            } else {
                                return Uni.I18n.translate('general.no', 'MDC', 'No')
                            }
                        }

                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
