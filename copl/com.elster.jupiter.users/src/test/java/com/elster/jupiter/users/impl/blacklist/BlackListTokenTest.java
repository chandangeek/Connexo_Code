/*
 *
 *  * Copyright (c) 2020  by Honeywell International Inc. All Rights Reserved
 *
 *
 */

package com.elster.jupiter.users.impl.blacklist;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.blacklist.BlackListToken;
import com.elster.jupiter.users.blacklist.BlackListTokenService;
import com.elster.jupiter.users.blacklist.impl.BlackListTokenServiceImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Insert your comments here.
 *
 * @author E492165 (M R)
 * @since 1/6/2020 (22:04)
 */

@RunWith(MockitoJUnitRunner.class)
public class BlackListTokenTest {

    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;

    private String COMPONENTNAME = "BLT";

    @Test
   public void blackListTokenTest(){
        when(ormService.newDataModel(COMPONENTNAME, "black_list_token")).thenReturn(dataModel);

       BlackListTokenServiceImpl instance = getInstance();
        instance.setOrmService(ormService);

        BlackListTokenService.BlackListTokenBuilder blackListTokenBuilder = instance.getBlackListTokenService();
    }

    private BlackListTokenServiceImpl getInstance(){
        return new BlackListTokenServiceImpl();
    }
}
