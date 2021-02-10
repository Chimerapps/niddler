package com.chimerapps.niddler.core;

/**
 * @author Nicola Verbeeck
 * @version 1
 */
public final class JavaNiddler extends Niddler implements Niddler.PlatformNiddler {

	private JavaNiddler() {
		super();
	}

	@Override
	public void closePlatform() {
	}

	public static class Builder extends Niddler.Builder<JavaNiddler> {

		public Builder() {
		}

		@Override
		public JavaNiddler build() {
			return new JavaNiddler();
		}
	}

}
