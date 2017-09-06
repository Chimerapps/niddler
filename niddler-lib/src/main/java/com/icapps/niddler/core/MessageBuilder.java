package com.icapps.niddler.core;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.icapps.niddler.util.Logging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
final class MessageBuilder {

	private MessageBuilder() {
		//Utility class
	}

	static String buildMessage(final NiddlerRequest request) {
		final JSONObject object = buildMessageJson(request);
		if (object == null) {
			return null;
		}
		return object.toString();
	}

	@Nullable
	private static JSONObject buildMessageJson(final NiddlerRequest request) {
		if (request == null) {
			return null;
		}
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "request");
			initGeneric(object, request);
			object.put("method", request.getMethod());
			object.put("url", request.getUrl());
		} catch (final JSONException e) {
			if (Logging.DO_LOG) {
				Log.e("MessageBuilder", "Failed to create json: ", e);
			}
			return null;
		}
		return object;
	}

	static String buildMessage(final NiddlerResponse response) {
		final JSONObject object = buildMessageJson(response);
		if (object == null) {
			return null;
		}
		return object.toString();
	}

	@Nullable
	private static JSONObject buildMessageJson(final NiddlerResponse response) {
		if (response == null) {
			return null;
		}
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "response");
			initGeneric(object, response);
			object.put("statusCode", response.getStatusCode());
			object.put("networkRequest", buildMessageJson(response.actualNetworkRequest()));
			object.put("networkReply", buildMessageJson(response.actualNetworkReply()));
		} catch (final JSONException e) {
			if (Logging.DO_LOG) {
				Log.e("MessageBuilder", "Failed to create json: ", e);
			}
			return null;
		}
		return object;
	}

	static String buildMessage(final Niddler.NiddlerServerInfo serverInfo) {
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "serverInfo");
			object.put("serverName", serverInfo.name);
			object.put("serverDescription", serverInfo.description);
		} catch (final JSONException e) {
			if (Logging.DO_LOG) {
				Log.e("MessageBuilder", "Failed to create json: ", e);
			}
			return "";
		}
		return object.toString();
	}

	static String buildMessage(final ServerAuth.AuthRequest request) {
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "authRequest");
			object.put("hash", request.hashKey);
			if (!TextUtils.isEmpty(request.packageName)) {
				object.put("package", request.packageName);
			}
		} catch (final JSONException e) {
			if (Logging.DO_LOG) {
				Log.e("MessageBuilder", "Failed to create json: ", e);
			}
			return "";
		}
		return object.toString();
	}

	static String buildAuthSuccess() {
		return "{\"type\":\"authSuccess\"}";
	}

	private static void initGeneric(final JSONObject object, final NiddlerMessageBase base) throws JSONException {
		object.put("messageId", base.getMessageId());
		object.put("requestId", base.getRequestId());
		object.put("timestamp", base.getTimestamp());
		object.put("headers", createHeadersObject(base));
		object.put("body", createBody(base));
	}

	private static String createBody(final NiddlerMessageBase base) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			base.writeBody(out);
		} catch (final IOException e) {
			if (Logging.DO_LOG) {
				Log.i("MessageBuilder", "Failed to write body", e);
			}
			return null;
		}
		final byte[] bytes = out.toByteArray();
		if (bytes == null) {
			return null;
		}
		return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
	}

	private static JSONObject createHeadersObject(final NiddlerMessageBase base) throws JSONException {
		final Map<String, List<String>> headers = base.getHeaders();
		if (headers == null || headers.isEmpty()) {
			return null;
		}

		final JSONObject object = new JSONObject();
		for (final Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
			final JSONArray array = new JSONArray();
			for (final String s : headerEntry.getValue()) {
				array.put(s);
			}
			object.put(headerEntry.getKey().toLowerCase(Locale.getDefault()), array);
		}
		return object;
	}

	static String buildProtocolVersionMessage() {
		return "{\"type\":\"protocol\",\"protocolVersion\":" + Niddler.NiddlerServerInfo.PROTOCOL_VERSION + "}";
	}
}
