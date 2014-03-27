package com.elster.jupiter.users.rest;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UserInfos {

    public int total;

    public List<UserInfo> users = new ArrayList<>();

    public UserInfos() {
    }

    public UserInfos(User user, TransactionService transactionService) {
        add(user, transactionService);
    }

    public UserInfos(Iterable<? extends User> users, TransactionService transactionService) {
        addAll(users, transactionService);
    }

    public UserInfo add(User user, TransactionService transactionService) {
        UserInfo result = new UserInfo(user, transactionService);
        users.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends User> users, TransactionService transactionService) {
        for (User each : users) {
            add(each, transactionService);
        }
    }

}
