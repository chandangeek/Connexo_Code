Ext.define('Usr.controller.Home', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.model.BreadcrumbItem'
    ],
    views: [
        'Home'
    ],

    showOverview: function () {
        var widget = Ext.widget('Home');

        location.href = '#home';
        this.getApplication().fireEvent('changecontentevent', widget);

        widget.down('#usersLink').autoEl.href = '#/users';
        widget.down('#groupsLink').autoEl.href = '#/groups';
    },

    signout: function (button) {
        var request = Ext.Ajax.request({
            url: '/apps/usr/index.html',
            method: 'GET',
            params: {
                logout: 'true'
            },
            scope: this,
            success: function () {
                window.location.replace('/apps/usr/index.html');
            }
        });
    }
});