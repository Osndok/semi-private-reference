package com.github.osndok.spr;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by robert on 2015-10-25 01:44.
 */
//@javax.module.CommandLineTool(name="spr1-key")
public
class Spr1Key extends Spr1Fragment
{
	private
	String privateString;

	public
	Spr1Key(String s)
	{
		super(s);

		try
		{
			if (s.startsWith(SUGGESTED_PREFIX))
			{
				privateString = s.substring(NUM_SHA1_B64_BYTES + SUGGESTED_PREFIX.length(), 2 * NUM_SHA1_B64_BYTES+ SUGGESTED_PREFIX.length());
			}
			else
			{
				privateString = s.substring(NUM_SHA1_B64_BYTES, 2*NUM_SHA1_B64_BYTES);
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new IllegalArgumentException("does not look like an spr1 reference: "+s, e);
		}

		assert(privateString.length()==NUM_SHA1_B64_BYTES);
	}

	private
	byte[] privateBytes;

	public
	Spr1Key(byte[] publicBytes, byte[] privateBytes)
	{
		super(publicBytes);

		MustLookLike.aSha1HashCode(privateBytes);

		//TODO: should we copy the array?
		this.privateBytes=privateBytes;
	}

	public
	String getPrivateString()
	{
		if (privateString==null)
		{
			privateString = Base64.encodeBase64URLSafeString(privateBytes);
		}

		return privateString;
	}

	// TODO: rename this to getPrivateHashBytes()
	public
	byte[] getPrivateBytes()
	{
		if (privateBytes==null)
		{
			privateBytes = Base64.decodeBase64(privateString);
		}

		return privateBytes;
	}

	public
	String getPrivateSha1()
	{
		return SHA1_PREFIX+ Hex.encodeHexString(getPrivateBytes());
	}

	public
	String getPrivateSha1Hex()
	{
		return Hex.encodeHexString(getPrivateBytes());
	}

	@Override
	public
	String toString()
	{
		return SUGGESTED_PREFIX+getPublicString()+getPrivateString();
	}

	public
	void writeTo(final OutputStream out) throws IOException
	{
		out.write(getPublicBytes());
		out.write(getPrivateBytes());
	}
}
