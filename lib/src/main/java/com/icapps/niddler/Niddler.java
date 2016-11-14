package com.icapps.niddler;

/**
 * @author Nicola Verbeeck
 * @date 10/11/16.
 */
public final class Niddler {

    private final int mPort;

    private Niddler(int port) {
        mPort = port;
    }

    public void logRequest(final NiddlerRequest request) {

    }

    public void logResponse(final NiddlerResponse response) {

    }

    public static class Builder {

        private int mPort = 6555;

        public Builder setPort(final int port) {
            mPort = port;
            return this;
        }

        public Niddler build() {
            return new Niddler(mPort);
        }

    }

}
