package com.allogy.spr;

import com.emstlk.nacl4s.crypto.Utils;
import com.emstlk.nacl4s.crypto.core.Salsa20;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Created by robert on 2015-10-25 13:52.
 */
public
class Spr1Encryption
{
	//BUG: seems to segfault whenever we try and get "the second block"... or hang forever :(
	private static final boolean USE_JNA_LIBSODIUM = Boolean.getBoolean("SPR1_JAVA_USE_LIBSODIUM");

	private final
	byte[] nonce;

	private final
	byte[] key;

	public static boolean DEBUG = false;

	/*
	NB: This mechanism requires to indeterminism, the use of Random is PURELY to help debugging & confidence.
	 */
	private static final
	Random random = new Random();

	/**
	 * @deprecated to discourage people from using it, this is intended for testing
	 * @param key - the Salsa20 key
	 * @param nonce - the Salsa20 nonce
	 */
	@Deprecated
	public
	Spr1Encryption(byte[] key, byte[] nonce)
	{
		this.key = key;
		this.nonce = nonce;
	}

	public
	Spr1Encryption(byte[] sha1)
	{
		MustLookLike.aSha1HashCode(sha1);

		this.key = new byte[32];
		this.nonce = new byte[8];

		if (DEBUG)
		{
			//In case we get our array indexing wrong, this should let some random bytes 'shine through' and produce
			//a noticeable CAS stability failure.
			random.nextBytes(this.key);
			random.nextBytes(this.nonce);
		}

		//NB: See SPR1 specification for where these numbers come from.
		System.arraycopy(sha1, 0, key, 0, 20);
		System.arraycopy(sha1, 0, key, 20, 12);
		System.arraycopy(sha1, 12, nonce, 0, 8);
	}

	public
	Spr1Encryption(Spr1Key spr1Key)
	{
		this(spr1Key.getPrivateBytes());
	}

	public
	byte[] encrypt(byte[] input)
	{
		final
		byte[] output=new byte[input.length];
		{
			salsa20stream(input, output, input.length, 0);
		}

		return output;
	}

	public
	byte[] decrypt(byte[] input)
	{
		final
		byte[] output=new byte[input.length];
		{
			salsa20stream(input, output, input.length, 0);
		}

		return output;
	}

	private
	int bufferSize = 4096;

	public
	int getBufferSize()
	{
		return bufferSize;
	}

	public
	void setBufferSize(int bufferSize)
	{
		this.bufferSize = bufferSize;
	}

	public
	StreamResult encrypt(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		return stream(inputStream, outputStream);
	}

	public
	StreamResult decrypt(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		return stream(inputStream, outputStream);
	}

	/*
	Given some bytes and any byte-level-offset in the stream, produce some encrypted (or decrypted) output.
	 */
	public
	void salsa20stream(byte[] input, byte[] output, int length, int streamOffset)
	{
		//NB: off-by-one to avoid reduplicated code (it is ++1 once the loop starts at zero).
		int blockNumber=(streamOffset/64)-1;

		byte[] block=null;

		for (int i=0; i<length; i++)
		{
			final
			int blockIndex=(streamOffset+i)%64;

			if (blockIndex==0 || block==null)
			{
				blockNumber++;
				block=getSalsa20Block(blockNumber);
			}

			output[i]=(byte)(input[i]^block[blockIndex]);
		}
	}

	public
	byte[] getSalsa20Block(long u)
	{
		if (USE_JNA_LIBSODIUM)
		{
			final
			byte[] zeroMessageBlock = new byte[64];

			//NB: For some reason, allocate() [as opposed to allocateDirect()] makes the JNA block forever (depending on the version)
			final
			ByteBuffer outputBlock = ByteBuffer.allocateDirect(64);
			{
				System.err.println("Descending into native library...");
				LibSodium.crypto_stream_salsa20_xor_ic(outputBlock, zeroMessageBlock, 512, nonce, u, key);
				System.err.println("Came back...");
			}

			//dnw: return outputBlock.array();
			//Double buffered?
			final
			byte[] retval = new byte[64];
			{
				outputBlock.get(retval);
			}

			return retval;
		}
		else
		{
			final
			byte[] iv=new byte[16];
			{
				System.arraycopy(nonce, 0, iv, 0, 8);

				for (int i = 8; i < 16; ++i)
				{
					iv[i] = (byte) (u & 0xff);
					u >>>= 8;
				}
			}

			final
			byte[] out = new byte[64];
			{
				Salsa20.cryptoCore(out, iv, key, Utils.getSigma());
			}

			return out;
		}
	}

	private
	StreamResult stream(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		final
		MessageDigest preHash;

		final
		MessageDigest postHash;
		{
			try
			{
				preHash=MessageDigest.getInstance("SHA-1");
				postHash=MessageDigest.getInstance("SHA-1");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new AssertionError(e);
			}
		}

		final
		int bufferSize = this.bufferSize;

		final
		byte[] buffer = new byte[bufferSize];

		int streamOffset = 0;

		int n;

		while ((n = inputStream.read(buffer)) > 0)
		{
			salsa20stream(buffer, buffer, n, streamOffset);
			outputStream.write(buffer, 0, n);
			streamOffset += n;
		}

		return new StreamResult(preHash.digest(), postHash.digest());
	}

	public static
	class StreamResult
	{
		private final
		byte[] preHash;

		private final
		byte[] postHash;

		public
		StreamResult(byte[] preHash, byte[] postHash)
		{
			this.preHash = preHash;
			this.postHash = postHash;
		}

		public
		byte[] getPreHash()
		{
			return preHash;
		}

		public
		byte[] getPostHash()
		{
			return postHash;
		}
	}
}
