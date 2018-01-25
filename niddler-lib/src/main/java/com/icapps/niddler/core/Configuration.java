package com.icapps.niddler.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Nicola Verbeeck
 * @version 1
 */

public class Configuration {

	@NonNull
	public final String debugClient;
	@NonNull
	public final BlacklistConfiguration blacklistConfiguration;
	@NonNull
	public final DebugConfiguration debugConfiguration;

	@NonNull
	private final ServerConnection mServerConnection;

	private Configuration(@NonNull final BlacklistConfiguration blacklistConfiguration,
			@NonNull final DebugConfiguration debugConfiguration,
			@NonNull final String debugServer,
			@NonNull final ServerConnection serverConnection) {
		this.blacklistConfiguration = blacklistConfiguration;
		this.debugConfiguration = debugConfiguration;
		this.debugClient = debugServer;
		mServerConnection = serverConnection;
	}

	public boolean isActive() {
		return mServerConnection.canReceiveData();
	}

	static Configuration fromJson(@NonNull final ServerConnection connection, @NonNull final JSONObject json) {
		try {
			return new Configuration(parseBlacklist(json.optJSONObject("blacklist")),
					parseDebugConfiguration(json.optJSONObject("debug"), connection),
					json.getString("debugClient"), connection);
		} catch (final JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	public static class BlacklistConfiguration {
		BlacklistConfiguration(@NonNull final List<Pattern> regex) {
			this.regularExpressions = regex;
		}

		@NonNull
		public final List<Pattern> regularExpressions;
	}

	public static class DebugConfiguration {
		DebugConfiguration(@NonNull final List<DebugAction> actions) {
			this.debugActions = actions;
		}

		@NonNull
		public final List<DebugAction> debugActions;
	}

	public static abstract class DebugAction {

		DebugAction(@NonNull final String urlRegex, @NonNull final ServerConnection serverConnection) {
			this.urlRegex = Pattern.compile(urlRegex);
			mServerConnection = serverConnection;
		}

		public boolean handles(@NonNull final NiddlerRequest request) {
			return mServerConnection.canReceiveData() && urlRegex.matcher(request.getUrl()).matches();
		}

		@Nullable
		public abstract DebugResponse handle(@NonNull final NiddlerRequest request) throws IOException;

		@NonNull
		public final Pattern urlRegex;

		@NonNull
		private final ServerConnection mServerConnection;
	}

	static class DebugDefaultReplyAction extends DebugAction {

		DebugDefaultReplyAction(@NonNull final String urlRegex, @NonNull final DebugResponse response, @NonNull final ServerConnection serverConnection) {
			super(urlRegex, serverConnection);
			this.response = response;
		}

		@Nullable
		@Override
		public DebugResponse handle(@NonNull final NiddlerRequest request) {
			return response;
		}

		@NonNull
		private DebugResponse response;
	}

	public static class DebugResponse {
		public final int code;

		@NonNull
		public final String message;

		@Nullable
		public final Map<String, String> headers;

		@Nullable
		public final String encodedBody;

		@Nullable
		public final String bodyMimeType;

		DebugResponse(final int code,
				@NonNull final String message,
				@Nullable final Map<String, String> headers,
				@Nullable final String encodedBody,
				@Nullable final String bodyMimeType) {
			this.code = code;
			this.message = message;
			this.headers = headers;
			this.encodedBody = encodedBody;
			this.bodyMimeType = bodyMimeType;
		}
	}

	@NonNull
	private static BlacklistConfiguration parseBlacklist(@Nullable final JSONObject blackList) {
		if (blackList == null) {
			return new BlacklistConfiguration(Collections.<Pattern>emptyList());
		}

		final JSONArray array = blackList.optJSONArray("regex");
		if (array == null) {
			return new BlacklistConfiguration(Collections.<Pattern>emptyList());
		}

		final int size = array.length();
		final List<Pattern> regularExpressions = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			final String item = array.optString(i);
			if (item != null) {
				regularExpressions.add(Pattern.compile(item));
			}
		}
		return new BlacklistConfiguration(regularExpressions);
	}

	@NonNull
	private static DebugConfiguration parseDebugConfiguration(@Nullable final JSONObject configuration, @NonNull final ServerConnection serverConnection) {
		if (configuration == null) {
			return new DebugConfiguration(Collections.<DebugAction>emptyList());
		}

		final JSONArray array = configuration.optJSONArray("actions");
		if (array == null) {
			return new DebugConfiguration(Collections.<DebugAction>emptyList());
		}

		final int size = array.length();
		final List<DebugAction> actions = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			final JSONObject item = array.optJSONObject(i);
			if (item == null) {
				continue;
			}

			final String regex = item.optString("regex");
			final String type = item.optString("type");
			if (regex == null || type == null) {
				continue;
			}

			switch (type) {
				case "reply":
					actions.add(new DebugDefaultReplyAction(regex, parseReply(item), serverConnection));
					break;
				default:
					continue;
			}
		}
		return new DebugConfiguration(actions);
	}

	@NonNull
	private static DebugResponse parseReply(@NonNull final JSONObject config) {
		try {
			return new DebugResponse(config.getInt("code"),
					config.getString("message"),
					parseHeaders(config.optJSONObject("headers")),
					config.optString("encodedBody"),
					config.optString("bodyMimeType"));
		} catch (final JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	@Nullable
	private static Map<String, String> parseHeaders(@Nullable final JSONObject headersObject) throws JSONException {
		if (headersObject == null) {
			return null;
		}

		final Map<String, String> headers = new HashMap<>();

		final Iterator<String> keys = headersObject.keys();
		while (keys.hasNext()) {
			final String key = keys.next();
			final String value = headersObject.getString(key);
			headers.put(key, value);
		}

		return headers;
	}
}
