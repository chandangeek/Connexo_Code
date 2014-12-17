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
        if (this.device.get('hasLoadProfiles')) {
            this.add({
                text: Uni.I18n.translate('devicemenu.channels', 'MDC', 'Channels'),
                itemId: 'channelsLink',
                href: '#/devices/' + mRID + '/channels',
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
        if (this.device.get('hasLogBooks')) {
            this.add({
                text: Uni.I18n.translate('devicemenu.events', 'MDC', 'Events'),
                itemId: 'events',
                href: '#/devices/' + mRID + '/events',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
            this.add({
                text: Uni.I18n.translate('deviceconfigurationmenu.generalAttributes', 'MDC', 'General attributes'),
                itemId: 'deviceGeneralAttributesLink',
                href: '#/devices/' + mRID + '/generalattributes',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.connectionMethods', 'MDC', 'Connection methods'),
                itemId: 'connectionMethodsLink',
                href: '#/devices/' + mRID + '/connectionmethods',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.protocols', 'MDC', 'Protocol dialects'),
                itemId: 'protocolLink',
                href: '#/devices/' + mRID + '/protocols',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.communicationTasks', 'MDC', 'Communication tasks'),
                itemId: 'communicationTasksLink',
                href: '#/devices/' + mRID + '/communicationtasks',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.communicationPlanning', 'MDC', 'Communication planning'),
                itemId: 'communicationSchedulesLink',
                href: '#/devices/' + mRID + '/communicationplanning',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication',
            'execute.device.message.level1','execute.device.message.level3','execute.device.message.level2','execute.device.message.level4'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.commands', 'MDC', 'Commands'),
                itemId: 'deviceCommands',
                href: '#/devices/' + mRID + '/commands',
                hrefTarget: '_self'
            });
        }
        if ((this.device.get('hasLogBooks') || this.device.get('hasLoadProfiles') || this.device.get('hasRegisters')) && Uni.Auth.hasAnyPrivilege(['privilege.administrate.validationConfiguration','privilege.view.validationConfiguration','privilege.view.fineTuneValidationConfiguration.onDevice'])) {
            this.add({
                text: Uni.I18n.translate('devicemenu.dataValidation', 'MDC', 'Data validation'),
                itemId: 'dataValidationLink',
                href: '#/devices/' + mRID + '/datavalidation',
                hrefTarget: '_self'
            });
        }
        if (Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication',
            'view.device.security.properties.level1','view.device.security.properties.level2','view.device.security.properties.level3','view.device.security.properties.level4',
            'edit.device.security.properties.level1','edit.device.security.properties.level2','edit.device.security.properties.level3','edit.device.security.properties.level4'])){
            this.add({
                text: Uni.I18n.translate('devicemenu.security', 'MDC', 'Security settings'),
                itemId: 'securitySettingLink',
                href: '#/devices/' + mRID + '/securitysettings',
                hrefTarget: '_self'
            });
        }
        if (this.device.get('gatewayType') === 'LAN' && Uni.Auth.hasAnyPrivilege(['privilege.view.device','privilege.administrate.deviceCommunication','privilege.operate.deviceCommunication'])) {
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
