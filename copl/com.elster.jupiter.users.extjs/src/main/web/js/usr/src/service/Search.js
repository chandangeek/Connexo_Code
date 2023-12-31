/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.service.Search', {

    extend: 'Uni.service.Search',

    init: function () {
        var me = this;

        me.defaultColumns = {
            'com.elster.jupiter.users.User': ['authenticationName', 'description', 'domain', 'active']
        };

        me.callParent(arguments);
    },

    createColumnDefinitionFromModel: function (field) {
        var me = this,
            column = this.callParent(arguments);

        if (column && column.dataIndex === 'name') {
            if (me.searchDomain.getId() === 'com.elster.jupiter.metering.EndDevice') {
                column.renderer = function (value, metaData) {
                    var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                };
            } else if (me.searchDomain.getId() === 'com.elster.jupiter.users.User') {
                column.renderer = function (value, metaData) {
                    var url = value;
                    metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                    return Ext.String.format('<a href="{0}">{1}</a>', url, Ext.String.htmlEncode(value));
                };
            }
        } else if (column.xtype != 'uni-date-column'
            && column.xtype != 'uni-grid-column-search-boolean'
            && column.xtype != 'uni-grid-column-search-devicetype'
            && column.xtype != 'uni-grid-column-search-deviceconfiguration'
            && column.xtype != 'uni-grid-column-search-quantity') {
            column.renderer = function (value, metaData, record) {

                if (column.header === Uni.I18n.translate('general.username', 'USR', 'Username')) {
                    var isUserLocked = record.raw.isUserLocked;
                    if (isUserLocked) {
                        value = '<span>' + record.get('authenticationName') + '</span><span class="icon-lock2" style="display:inline-block; color:rgba(255, 0, 0, 0.3);position:relative; left:5px;"></span>';
                        return value;
                    }
                }

                // Special case for status column
                if (column.header === Uni.I18n.translate('user.status', 'USR', 'Status')) {
                    if (value === 'true') {
                        value = Uni.I18n.translate('general.active', 'USR', 'Active');
                    } else {
                        value = Uni.I18n.translate('general.inactive', 'USR', 'Inactive');
                    }
                }

                if (column.header === Uni.I18n.translate('user.locked', 'USR', 'Locked')) {
                    if (value === 'true') {
                        value = Uni.I18n.translate('general.userLocked', 'USR', 'Yes');
                    } else {
                        value = Uni.I18n.translate('general.userUnlocked', 'USR', 'No');
                    }
                }

                // Special case for language column
                if (column.header === Uni.I18n.translate('user.language', 'USR', 'Language')) {
                    if (value) {
                        value = value.languageTag;
                    }
                }

                // stupid solution to resolve encoding in tooltip
                metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(value)) + '"';
                return Ext.String.htmlEncode(value) || '-';
            };
        }

        column.menuDisabled = true;
        return column;
    },

    createFixedColumns: function () {
        var actionColumn = {
            xtype: 'uni-actioncolumn',
            header: Uni.I18n.translate('general.actions', 'USR', 'Actions'),
            maxWidth: 120,
            privileges: Usr.privileges.Users.adminUsers,
            menu: {
                xtype: 'user-action-menu'
            },
            isDefault: true
        };

        return [actionColumn];
    }
});
