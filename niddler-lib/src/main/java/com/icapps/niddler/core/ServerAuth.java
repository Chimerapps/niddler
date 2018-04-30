package com.icapps.niddler.core;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Nicola Verbeeck
 * Date 22/11/16.
 */
final class ServerAuth {

	private ServerAuth() {
		//Utility class
	}

	private static SecureRandom mRandom;

	private static void init() {
		if (mRandom != null) {
			return;
		}

		mRandom = new SecureRandom();
	}

	static AuthRequest generateAuthenticationRequest(@Nullable final String packageName) {
		init();
		final byte[] randomBytes = new byte[512];
		mRandom.nextBytes(randomBytes);
		return new AuthRequest(Base64.encodeToString(randomBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP), packageName);
	}

	static boolean checkAuthReply(final AuthRequest request, final AuthReply reply, final String password) {
		if (reply == null || reply.hashKey == null || request == null) {
			return false;
		}
		try {
			final String mustBe = Base64.encodeToString(MessageDigest.getInstance("SHA-512").digest((request.hashKey + password).getBytes("UTF-8")),
					Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
			return reply.hashKey.equals(mustBe);
		} catch (final NoSuchAlgorithmException e) {
			Log.e("ServerAuth", "SHA-512 not found", e);

			return false;
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not found, BAIL", e);
		}
	}

	static class AuthRequest {
		final String hashKey;
		@Nullable
		final String packageName;

		AuthRequest(final String hashKey, @Nullable final String packageName) {
			this.hashKey = hashKey;
			this.packageName = packageName;
		}
	}

	static class AuthReply {
		final String hashKey;

		AuthReply(final String hashKey) {
			this.hashKey = hashKey;
		}
	}

}
