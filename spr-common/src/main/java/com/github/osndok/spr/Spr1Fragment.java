package com.github.osndok.spr;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

/**
 * A "partial" key, that only includes the public/outer hash value.
 *
 * Created by robert on 2015-10-25 01:03.
 */
//@javax.module.CommandLineTool(name="spr1-fragment")
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


	private
	Long sizeHint;

	private
	String sizeHintString;

	private
	byte[] sizeHintBytes;

	Spr1Fragment()
	{
		//for testing...
	}

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

		if (publicString.charAt(2)=='-')
		{
			this.sizeHintString=publicString.substring(0, 2);
			publicString=publicString.substring(3);
		}

		assert(publicString.length()==NUM_SHA1_B64_BYTES);
		this.publicString = publicString;
	}

	public
	Spr1Fragment(byte[] publicBytes)
	{
		//TODO: if we have an extra two bytes, pop off the first two for the size hint.
		MustLookLike.aSha1HashCode(publicBytes);

		//TODO: should we copy this array?
		this.publicBytes = publicBytes;
	}

	//@javax.module.CommandLineOption(_long = "public", _short = 'u')
	public
	String getPublicString()
	{
		if (publicString==null)
		{
			publicString = Base64.encodeBase64URLSafeString(publicBytes);
		}

		return publicString;
	}

	// TODO: rename this to getPublishHashBytes()
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
		return SHA1_PREFIX+getPublicSha1Hex();
		//return SUGGESTED_PREFIX+getPublicString()+"????";
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

	public
	void setPublicString(String publicString)
	{
		this.publicBytes = null;
		this.publicString = publicString;
	}

	public
	void setPublicBytes(byte[] publicBytes)
	{
		this.publicString=null;
		this.publicBytes = publicBytes;
	}

	public
	String getSizeHintString()
	{
		if (sizeHintString==null)
		{
			if (sizeHint!=null)
			{
				int power=(int)Math.ceil(Math.log(sizeHint)/Math.log(2)*37.0);
				sizeHintString=new Base47i().encodeInt(power);
			}
			else
			if (sizeHintBytes != null)
			{
				int power=unsigned(sizeHintBytes[0])<<8 | unsigned(sizeHintBytes[1]);
				sizeHintString=new Base47i().encodeInt(power);
			}
		}
		return sizeHintString;
	}

	public
	void setSizeHintString(String sizeHintString)
	{
		this.sizeHint=null;
		this.sizeHintBytes=null;
		this.sizeHintString = sizeHintString;
	}

	public
	byte[] getSizeHintBytes()
	{
		if (sizeHintBytes==null)
		{
			if (sizeHint!=null)
			{
				int power=(int)Math.ceil(Math.log(sizeHint)/Math.log(2)*37.0);
				sizeHintBytes=new byte[]{(byte)(power>>8), (byte)(power)};
			}
			else
			if (sizeHintString!=null)
			{
				int power=new Base47i().decodeInt(sizeHintString);
				sizeHintBytes=new byte[]{(byte)(power>>8), (byte)(power)};
			}
		}
		return sizeHintBytes;
	}

	public
	void setSizeHintBytes(byte[] sizeHintBytes)
	{
		this.sizeHint=null;
		this.sizeHintString=null;
		this.sizeHintBytes = sizeHintBytes;
	}

	public
	Long getSizeHint()
	{
		if (sizeHint==null)
		{
			if (sizeHintString!=null)
			{
				int power=new Base47i().decodeInt(sizeHintString);
				sizeHint=(long)Math.floor(Math.pow(2.0, (power/37.0)));
			}
			else
			if (sizeHintBytes!=null)
			{
				int power=unsigned(sizeHintBytes[0])<<8 | unsigned(sizeHintBytes[1]);
				sizeHint=(long)Math.floor(Math.pow(2.0, (power/37.0)));
			}
		}
		return sizeHint;
	}

	private
	int unsigned(byte b)
	{
		return 0xff & ((int)b);
	}

	public
	void setSizeHint(long sizeHint)
	{
		this.sizeHintBytes=null;
		this.sizeHintString=null;
		this.sizeHint = sizeHint;
	}
}
