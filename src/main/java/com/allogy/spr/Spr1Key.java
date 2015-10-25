package com.allogy.spr;

import org.apache.commons.codec.binary.Base64;

import javax.module.CommandLineTool;

/**
 * Created by robert on 2015-10-25 01:44.
 */
@CommandLineTool(name="spr1key")
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

	public
	byte[] getPrivateBytes()
	{
		if (privateBytes==null)
		{
			privateBytes = Base64.decodeBase64(privateString);
		}

		return privateBytes;
	}

	@Override
	public
	String toString()
	{
		return SUGGESTED_PREFIX+getPublicString()+getPrivateString();
	}
}
