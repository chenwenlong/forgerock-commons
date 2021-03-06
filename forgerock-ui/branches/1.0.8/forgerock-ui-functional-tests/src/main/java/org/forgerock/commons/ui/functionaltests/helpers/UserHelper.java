package org.forgerock.commons.ui.functionaltests.helpers;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;
import org.forgerock.commons.ui.functionaltests.openidmclient.OpenIDMClient;
import org.forgerock.commons.ui.functionaltests.utils.JsonUtils;
import org.springframework.stereotype.Component;

@Component
public class UserHelper {
	
	@Inject
	protected JsonUtils jsonUtils;
	
	@Inject
	protected SeleniumHelper seleniumHelper;
	
	@Inject
	private OpenIDMClient openIDMClient;
	
	public void login(String user, String password) {
		String notParsedCookie = openIDMClient.loginAndReturnCookie(user, password);
		String cookie = notParsedCookie.split(";")[0].split("=")[1];
		seleniumHelper.setSessionCookie(cookie);
	}
	
	public void logout() {
		seleniumHelper.removeCookies();
	}
	
	public String register(JsonNode user) {
		return openIDMClient.createUser(user);
	}

	public void createDefaultUser() {
		openIDMClient.createUser(jsonUtils.readJsonFromFile("/defaultuser.json"));
	}
	
	public void createDefaultAdminUser() {
		openIDMClient.createUser(jsonUtils.readJsonFromFile("/defaultadminuser.json"));
		openIDMClient.updateUserField("admin@test.test", "roles", "openidm-authorized,admin");
	}
	
	public void createSecondDefaultUser() {
		openIDMClient.createUser(jsonUtils.readJsonFromFile("/seconddefaultuser.json"));
	}

	public void loginAsDefaultUser() {
		login("test@test.test", "tesT#1#Test");
	}
	
	public void loginAsOpenIDMAdmin() {
		login("openidm-admin", "openidm-admin");
	}

	public void loginAsDefaultAdminUser() {
		login("admin@test.test", "tesT#1#Test");
	}
	
}
