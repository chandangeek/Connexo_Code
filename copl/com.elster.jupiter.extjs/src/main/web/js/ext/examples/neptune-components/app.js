/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

NeptuneAppData = {
    dummyText: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod ' +
        'tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis ' +
        'nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis ' +
        'aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat ' +
        'nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui ' + 
        'officia deserunt mollit anim id est laborum.'
};
Ext.application({
    name: 'Neptune',
    autoCreateViewport: true,
    controllers: ['Main']
});
