package io.github.ilmich.tempesta.web.http.auth;

public interface AuthDb {

	public AuthUser getUser(String username);

	public boolean userExists(String username);

}
