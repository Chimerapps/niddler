package com.icapps.niddler.core;

import android.support.annotation.NonNull;

import com.icapps.niddler.util.LogUtil;
import com.icapps.niddler.util.StringUtil;

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

	static String buildMessage(final NiddlerRequest request, final int stackTraceMaxDepth) {
		final JSONObject object = buildMessageJson(request, stackTraceMaxDepth);
		if (object == null) {
			return null;
		}
		return object.toString();
	}

	static JSONObject buildMessageJson(final NiddlerRequest request, final int stackTraceMaxDepth) {
		if (request == null) {
			return null;
		}
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "request");
			initGeneric(object, request);
			object.put("method", request.getMethod());
			object.put("url", request.getUrl());

			final StackTraceElement[] trace = request.getRequestStackTrace();
			if (trace != null) {
				final int end = Math.min(trace.length, stackTraceMaxDepth);
				if (end > 0) {
					for (int i = 0; i < end; ++i) {
						object.append("trace", (trace[i].toString()));
					}
				}
			}
			final List<String> context = request.getRequestContext();
			if (context != null && !context.isEmpty()) {
				for (final String contextItem : context) {
					object.append("context", contextItem);
				}
			}
		} catch (final JSONException e) {
			LogUtil.niddlerLogError("MessageBuilder", "Failed to create json: ", e);

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

	static JSONObject buildMessageJson(final NiddlerResponse response) {
		if (response == null) {
			return null;
		}
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "response");
			initGeneric(object, response);
			object.put("statusCode", response.getStatusCode());
			object.put("networkRequest", buildMessageJson(response.actualNetworkRequest(), 0));
			object.put("networkReply", buildMessageJson(response.actualNetworkReply()));
			object.put("writeTime", response.getWriteTime());
			object.put("readTime", response.getReadTime());
			object.put("waitTime", response.getWaitTime());
			object.put("httpVersion", response.getHttpVersion());
			object.put("statusLine", response.getStatusLine());
		} catch (final JSONException e) {
			LogUtil.niddlerLogError("MessageBuilder", "Failed to create json: ", e);

			return null;
		}
		return object;
	}

	@NonNull
	static String buildMessage(final Niddler.NiddlerServerInfo serverInfo) {
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "serverInfo");
			object.put("serverName", serverInfo.name);
			object.put("serverDescription", serverInfo.description);
		} catch (final JSONException e) {
			LogUtil.niddlerLogError("MessageBuilder", "Failed to create json: ", e);

			return "";
		}
		return object.toString();
	}

	@NonNull
	static String buildMessage(final ServerAuth.AuthRequest request) {
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "authRequest");
			object.put("hash", request.hashKey);
			if (!StringUtil.isEmpty(request.packageName)) {
				object.put("package", request.packageName);
			}
		} catch (final JSONException e) {
			LogUtil.niddlerLogError("MessageBuilder", "Failed to create json: ", e);

			return "";
		}
		return object.toString();
	}

	@NonNull
	static String buildAuthSuccess() {
		return "{\"type\":\"authSuccess\"}";
	}

	@NonNull
	static String buildMessage(@NonNull final String id, @NonNull final String name,
			@NonNull final List<Niddler.StaticBlackListEntry> blacklist) {
		if (blacklist.isEmpty()) {
			return "{\"type\":\"staticBlacklist\"}";
		}
		final JSONObject object = new JSONObject();
		try {
			object.put("type", "staticBlacklist");
			object.put("id", id);
			object.put("name", name);
			final JSONArray array = new JSONArray();
			for (final Niddler.StaticBlackListEntry blackListEntry : blacklist) {
				final JSONObject inner = new JSONObject();
				inner.put("pattern", blackListEntry.pattern());
				inner.put("enabled", blackListEntry.isEnabled());
				array.put(inner);
			}
			object.put("entries", array);
		} catch (final JSONException e) {
			LogUtil.niddlerLogError("MessageBuilder", "Failed to create json: ", e);
			return "";
		}
		return object.toString();
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
			LogUtil.niddlerLogError("MessageBuilder", "Failed to write body", e);

			return null;
		}
		final byte[] bytes = out.toByteArray();
		if (bytes == null) {
			return null;
		}
		return StringUtil.toString(bytes);
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
