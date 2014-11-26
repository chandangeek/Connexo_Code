Ext.define('Mdc.view.setup.device.DeviceMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceMenu',
    toggleId: null,
    device: null,

    initComponent: function () {
        var mRID = this.device.get('mRID');

        this.callParent(arguments);

        this.add({
            text: Uni.I18n.translate('devicemenu.overview', 'MDC', 'Overview'),
            itemId: 'deviceOverviewLink',
            href: '#/devices/' + mRID,
            hrefTarget: '_self'
        });
        if (this.device.get('hasRegisters')) {
            this.add({
                text: Uni.I18n.translate('devicemenu.registerTypes', 'MDC', 'Registers'),
                itemId: 'registersLink',
                href: '#/devices/' + mRID + '/registers',
                hrefTarget: '_self'
            });
        }
        if (this.device.get('hasLoadProfiles')) {
            this.add({
                text: Uni.I18n.translate('devicemenu.loadProfiles', 'MDC', 'Load profiles'),
                itemId: 'loadProfilesLink',
                href: '#/devices/' + mRID + '/loadprofiles',
                hrefTarget: '_self'
            });
        }
        if (this.device.get('hasLogBooks')) {
            this.add({
                text: Uni.I18n.translate('devicemenu.logbooks', 'MDC', 'Logbooks'),
                itemId: 'logbooksLink',
                href: '#/devices/' + mRID + '/logbooks',
                hrefTarget: '_self'
            });
        }
        this.add({
            text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
            itemId: 'connectionMethodsLink',
            href: '#/devices/' + mRID + '/connectionmethods',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
            itemId: 'protocolLink',
            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.protocol','privilege.view.protocol']),
            href: '#/devices/' + mRID + '/protocols',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.communicationTasks', 'MDC', 'Communication tasks'),
            itemId: 'communicationTasksLink',
            href: '#/devices/' + mRID + '/communicationtasks',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.communicationPlanning', 'MDC', 'Communication planning'),
            itemId: 'communicationSchedulesLink',
            href: '#/devices/' + mRID + '/communicationplanning',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.commands', 'MDC', 'Commands'),
            itemId: 'deviceCommands',
            href: '#/devices/' + mRID + '/commands',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.dataValidation', 'MDC', 'Data validation'),
            itemId: 'dataValidationLink',
            hidden: !Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration']),
            href: '#/devices/' + mRID + '/datavalidation',
            hrefTarget: '_self'
        });
        this.add({
            text: Uni.I18n.translate('devicemenu.security', 'MDC', 'Security settings'),
            itemId: 'securitySettingLink',
            href: '#/devices/' + mRID + '/securitysettings',
            hrefTarget: '_self'
        });
        if (this.device.get('gatewayType') === 'LAN') {
            this.add({
                text: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
                itemId: 'topologyLink',
                href: '#/devices/' + mRID + '/topology',
                hrefTarget: '_self'
            });
        }

        this.toggleByItemId(this.toggleId);
        this.setTitle(mRID);
    },

    toggleByItemId: function (toggleId) {
        var cls = this.selectedCls;
        var item = this.down('#' + toggleId);
        if (item.hasCls(cls)) {
            item.removeCls(cls);
        } else {
            item.addCls(cls);
        }
    }

});
