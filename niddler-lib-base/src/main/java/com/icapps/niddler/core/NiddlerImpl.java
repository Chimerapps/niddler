package com.icapps.niddler.core;

import android.support.annotation.NonNull;

import com.icapps.niddler.core.debug.NiddlerDebugger;
import com.icapps.niddler.util.LogUtil;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
class NiddlerImpl implements NiddlerServer.WebSocketListener {

    private static final String LOG_TAG = Niddler.class.getSimpleName();

    private NiddlerServer mServer;
    private Niddler.PlatformNiddler mPlatform;

    private final MessagesCache mMessageCache;
    private final Niddler.NiddlerServerInfo mNiddlerServerInfo;

    private boolean mIsStarted = false;
    private boolean mIsClosed = false;
    private String mStaticBlacklistMessage;


    protected NiddlerImpl(final String password, final int port, final long cacheSize, final Niddler.NiddlerServerInfo niddlerServerInfo,
                          final Niddler.StaticBlacklistListener blacklistListener) {
        try {
            mServer = new NiddlerServer(password, port, niddlerServerInfo.name, this, blacklistListener);
        } catch (final UnknownHostException ex) {
            LogUtil.niddlerLogError(LOG_TAG, "Failed to start server: " + ex.getLocalizedMessage());
        }
        mMessageCache = new MessagesCache(cacheSize);
        mNiddlerServerInfo = niddlerServerInfo;
        mStaticBlacklistMessage = MessageBuilder.buildMessage(Collections.<Niddler.StaticBlackListEntry>emptyList());
    }

    @Override
    public void onConnectionOpened(final WebSocket conn) {
        if (mNiddlerServerInfo != null) {
            conn.send(MessageBuilder.buildMessage(mNiddlerServerInfo));
        }
        for (final String message : mMessageCache.get()) {
            conn.send(message);
        }
        if (!mStaticBlacklistMessage.isEmpty()) {
            conn.send(mStaticBlacklistMessage);
        }
    }

    void start() {
        if ((mServer != null) && !mIsStarted) {
            mServer.start();
            mIsStarted = true;
            LogUtil.niddlerLogDebug(LOG_TAG, "Started niddler server on " + mServer.getAddress());
        }
    }

    void setPlatform(final Niddler.PlatformNiddler platform) {
        mPlatform = platform;
    }

    void close() throws IOException {
        final Niddler.PlatformNiddler platform = mPlatform;
        if (platform != null) {
            platform.closePlatform();
        }

        if (mServer != null) {
            try {
                mServer.stop();
            } catch (final InterruptedException e) {
                throw new IOException(e);
            } finally {
                mIsClosed = true;
                mMessageCache.clear();
            }
        }
    }

    NiddlerDebugger debugger() {
        return mServer.debugger();
    }

    boolean isStarted() {
        return mIsStarted;
    }

    boolean isClosed() {
        return mIsClosed;
    }

    void send(final String message) {
        if (mServer != null) {
            mMessageCache.put(message);
            mServer.sendToAll(message);
        }
    }

    void onStaticBlacklistChanged(@NonNull final List<Niddler.StaticBlackListEntry> blacklist) {
        mStaticBlacklistMessage = MessageBuilder.buildMessage(blacklist);
        if (isStarted() && !isClosed() && !mStaticBlacklistMessage.isEmpty()) {
            mServer.sendToAll(mStaticBlacklistMessage);
        }

    }

    int getPort() {
        return mServer.getPort();
    }

}
