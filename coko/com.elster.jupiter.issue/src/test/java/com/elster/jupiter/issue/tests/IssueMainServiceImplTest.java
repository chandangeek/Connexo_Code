package com.elster.jupiter.issue.tests;

import com.elster.jupiter.transaction.TransactionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IssueMainServiceImplTest extends BaseTest{

    @Test
    public void test(){
        try (TransactionContext context = getContext()) {

        }
    }

}
