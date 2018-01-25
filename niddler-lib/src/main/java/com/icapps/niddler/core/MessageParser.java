package com.icapps.niddler.core;

import org.json.JSONObject;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
final class MessageParser {

	private MessageParser() {
		//Utility class
	}

	static ServerAuth.AuthReply parseAuthReply(final JSONObject jsonObject) {
		return new ServerAuth.AuthReply(jsonObject.optString("hashKey"));
	}

	static Configuration parseConfiguration(final JSONObject jsonObject) {
		return Configuration.fromJson(jsonObject);
	}
}
