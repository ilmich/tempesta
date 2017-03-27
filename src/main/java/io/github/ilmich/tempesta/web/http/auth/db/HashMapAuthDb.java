package io.github.ilmich.tempesta.web.http.auth.db;

import java.util.Map;


import com.google.common.collect.Maps;

import io.github.ilmich.tempesta.web.http.auth.AuthDb;
import io.github.ilmich.tempesta.web.http.auth.AuthUser;

public class HashMapAuthDb implements AuthDb {

    private Map<String, AuthUser> users = Maps.newHashMap();

    public HashMapAuthDb addUser(String username, String password) {
	this.users.put(username, new AuthUser(username, password));
	return this;
    }

    public HashMapAuthDb addAll(Map<String, String> users) {
	for (String userName : users.keySet()) {
	    addUser(userName, users.get(userName));
	}
	return this;
    }

    @Override
    public AuthUser getUser(String username) {
	return this.users.get(username);
    }

    @Override
    public boolean userExists(String username) {
	return this.users.containsKey(username);
    }
}
