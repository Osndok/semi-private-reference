package com.allogy.spr;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.module.CommandLineOption;
import javax.module.CommandLineTool;
import java.util.Arrays;

/**
 * A "partial" key, that only includes the public/outer hash value.
 *
 * Created by robert on 2015-10-25 01:03.
 */
@CommandLineTool(name="spr1-fragment")
public
class Spr1Fragment
{
	public static final
	String SUGGESTED_PREFIX="spr1-";

	public static final
	int NUM_SHA1_B64_BYTES=27;

	public static final
	String SHA1_PREFIX="sha1-";

	private
	String publicString;

	private
	byte[] publicBytes;

	public
	Spr1Fragment(String publicString)
	{
		if (publicString==null)
		{
			throw new NullPointerException();
		}

		//What is the likelihood of a base64'd hash naturally starting with "spr1-"?
		//I don't know, but if this concerns you... then you should probably be using the byte constructor!!!!
		try
		{
			if (publicString.startsWith(SUGGESTED_PREFIX))
			{
				publicString=publicString.substring(SUGGESTED_PREFIX.length(), SUGGESTED_PREFIX.length()+NUM_SHA1_B64_BYTES);
			}
			else
			{
				publicString = publicString.substring(0, NUM_SHA1_B64_BYTES);
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new IllegalArgumentException("spr1 key fragment must be at least 27 base64 characters (plus optional [and ambiguous] 'spr1-' prefix): "+publicString, e);
		}

		assert(publicString.length()==NUM_SHA1_B64_BYTES);
		this.publicString = publicString;
	}

	public
	Spr1Fragment(byte[] publicBytes)
	{
		MustLookLike.aSha1HashCode(publicBytes);

		//TODO: should we copy this array?
		this.publicBytes = publicBytes;
	}

	@CommandLineOption(_long = "public", _short = 'u')
	public
	String getPublicString()
	{
		if (publicString==null)
		{
			publicString = Base64.encodeBase64URLSafeString(publicBytes);
		}

		return publicString;
	}

	public
	byte[] getPublicBytes()
	{
		if (publicBytes==null)
		{
			publicBytes = Base64.decodeBase64(publicString);
		}

		return publicBytes;
	}

	public
	String getPublicSha1()
	{
		return SHA1_PREFIX+Hex.encodeHexString(getPublicBytes());
	}

	public
	String getPublicSha1Hex()
	{
		return Hex.encodeHexString(getPublicBytes());
	}

	@Override
	public
	String toString()
	{
		return SUGGESTED_PREFIX+getPublicString()+"????";
	}

	@Override
	public final
	boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}

		if (!(o instanceof Spr1Fragment))
		{
			return false;
		}

		final
		Spr1Fragment that = (Spr1Fragment) o;

		if (publicString==null)
		{
			return Arrays.equals(publicBytes, that.getPublicBytes());
		}
		else
		{
			return publicString.equals(that.getPublicString());
		}
	}

	@Override
	public final
	int hashCode()
	{
		return getPublicString().hashCode();
	}
}
