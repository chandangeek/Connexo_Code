/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.controller.history.Workspace', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Ddv.controller.Validations'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        "workspace/validations": {
            title: Uni.I18n.translate('validation.validations.title', 'DDV', 'Validations'),
            route: 'workspace/validations',
            controller: 'Ddv.controller.Validations',
            action: 'showValidations'
        }
    },

    init: function () {
        var router = this.getController('Uni.controller.history.Router');
        router.addConfig(this.routeConfig);
    }
});
