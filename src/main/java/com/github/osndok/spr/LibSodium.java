package com.github.osndok.spr;

import com.sun.jna.Native;
import com.sun.jna.Memory;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * In order to work, this requires libsodium >= 0.6.0
 *
 * e.g. the version that comes in Fedora 21, or newer.
 *
 * Particularly because this is a "somewhat newer" version that is required, and in order to
 * support older operating systems, and in order to support processor-optimized builds... we
 * will check for a "/usr/local" version *first*.
 *
 * Created by robert on 2015-10-27 09:37.
 */
class LibSodium
{
	/*
	http://doc.libsodium.org/advanced/salsa20.html
	 */
	public static native
	int crypto_stream_salsa20_xor_ic(ByteBuffer outputBuffer, byte[] message,
									 long messageLength,
									 byte[] nonce, long blockCounter,
									 byte[] key);

	private static native
	int sodium_init();

	static
	{
		final
		File localOverride=new File("/usr/local/lib/libsodium.so");

		if (localOverride.exists())
		{
			System.err.println("Loading: "+localOverride);
			Native.register(localOverride.toString());
		}
		else
		{
			System.err.println("Loading libsodium...");
			Native.register("libsodium");
		}

		System.err.println("Initializing...");

		/*
		http://doc.libsodium.org/usage/index.html
		 */
		final
		int status=sodium_init();

		if (status==0)
		{
			System.err.println("Libsodium is ready.");
		}
		else
		{
			throw new IllegalStateException("unable to initialize libsodium; "+status);
		}
	}
}
