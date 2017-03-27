package io.github.ilmich.tempesta.web.http.auth.handlers;

import java.util.Map;

import io.github.ilmich.tempesta.web.http.auth.db.HashMapAuthDb;

public class MapHttpBasicAuthHandler extends HttpBasicAuthHandler {

    public MapHttpBasicAuthHandler(String realm) {
	super(realm);
	setAuthDb(new HashMapAuthDb());
    }

    public MapHttpBasicAuthHandler addUser(String username, String password) {
	((HashMapAuthDb) getAuthDb()).addUser(username, password);
	return this;
    }

    public MapHttpBasicAuthHandler addAll(Map<String, String> users) {
	((HashMapAuthDb) getAuthDb()).addAll(users);
	return this;
    }

}
