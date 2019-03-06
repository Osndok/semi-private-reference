package com.github.osndok.spr;

/**
 * An "intuitive" (easy-to-read and easy to re-enter) integer encoding for
 * values that will likely have to be transferred by means other than
 * copy-and-paste (such as by writing it down on paper, or being memorized),
 * or when two such numbers are expected to be comparable without help from
 * a computer (to just 'know' which value is larger by the number of digits
 * or the most significant differing digit).
 *
 * In particular, there are no visually ambiguous characters, and all of the
 * characters are "in the right order" (intuitively).
 *
 * Created by robert on 2017-01-12 10:45.
 */
public
class Base47i
{
	/**
	 * character *SET* is unambiguous characters (according to pwgen).
	 * character *ORDER* is:
	 * (1) numbers first (in order)
	 * (2) inter-case alphabet order (caseless compare, but with lowercase before upper)
	 */
	private static final
	char[] CHARACTERS ="3479aAbcCdeEfFghHijJkKLmMnNopPqrRstTuUvVwWxXyYz".toCharArray();

	private static final
	int C = 47;

	/**
	 * Encodes a 64-bit long value to a Base64a <code>String</code>.
	 *
	 * @param b10 the long value to encode
	 * @return the number encoded as a Base64a <code>String</code>.
	 */
	public
	String encodeLong(long b10)
	{
		//Since we switched to the hokey negation strategy, we must handle the MIN_VALUE separately...
		if (b10 == Long.MIN_VALUE)
		{
			return "-9tHnyKTCFk4u";
		}

		final
		StringBuilder ret = new StringBuilder(11);

		final
		boolean negate;
		{
			if (b10 < 0)
			{
				negate = true;
				b10 = -b10;
				assert(b10>0);
			}
			else
			{
				negate = false;
			}
		}

		do
		{
			final
			char c = CHARACTERS[(int)(b10 % C)];

			ret.insert(0, c);

			//BUG: "unsigned" right-shift is important, will eventually zero-out the input (even if negative).
			b10 /= C;
			assert(b10>=0);
		}
		while (b10 != 0);

		if (negate)
		{
			ret.insert(0, '-');
		}

		return ret.toString();
	}

	/**
	 * Encodes a 32-bit integer value to a Base64a <code>String</code>.
	 *
	 * @param b10 the integer value to encode
	 * @return the number encoded as a Base64a <code>String</code>.
	 */
	public
	String encodeInt(int b10)
	{
		//Since we switched to the hokey negation strategy, we must handle the MIN_VALUE separately...
		if (b10 == Integer.MIN_VALUE)
		{
			return "-dia9yK";
		}

		final
		StringBuilder ret = new StringBuilder(6);

		final
		boolean negate;
		{
			if (b10 < 0)
			{
				negate = true;
				b10 = -b10;
				assert(b10>0);
			}
			else
			{
				negate = false;
			}
		}

		//System.err.println("b10 = "+b10);
		do
		{
			final
			char c = CHARACTERS[(int)(b10 % C)];
			ret.insert(0, c);

			//NB: "unsigned" right-shift is important, will eventually zero-out the input (even if negative).
			b10 /= C;
			//System.err.println("b10 = "+b10);
			assert(b10>=0);
		}
		while (b10 != 0);

		if (negate)
		{
			ret.insert(0, '-');
		}

		return ret.toString();
	}


	/**
	 * Decodes a Base64a <code>String</code> returning a <code>long</code>.
	 *
	 * @param b64
	 * the Base64a <code>String</code> to decodeLong.
	 * @return the decoded number as a <code>long</code>.
	 * @throws IllegalArgumentException
	 * if the given <code>String</code> contains characters not
	 * specified in the constructor.
	 */
	public
	long decodeLong(String b64)
	{
		final
		boolean negate;
		{
			if (b64.charAt(0)=='-')
			{
				negate=true;
				b64=b64.substring(1);
			}
			else
			{
				negate=false;
			}
		}

		long ret = 0;
		b64 = new StringBuilder(b64).reverse().toString();

		long magnitude = 1;
		for (char character : b64.toCharArray())
		{
			int i = indexOf(character);
			if (i<0)
			{
				throw new IllegalArgumentException("Invalid character(s) in string: " + character);
			}

			ret += i * magnitude;
			magnitude *= C;
		}

		if (negate)
		{
			return -ret;
		}
		else
		{
			return ret;
		}
	}

	/**
	 * Decodes a Base64a <code>String</code> returning a <code>long</code>.
	 *
	 * @param b64
	 * the Base64a <code>String</code> to decodeLong.
	 * @return the decoded number as a <code>long</code>.
	 * @throws IllegalArgumentException
	 * if the given <code>String</code> contains characters not
	 * specified in the constructor.
	 */
	public
	int decodeInt(String b64)
	{
		final
		boolean negate;
		{
			final
			char firstChar=b64.charAt(0);

			if (firstChar=='-')
			{
				negate=true;
				b64=b64.substring(1);
			}
			else
			{
				negate=false;
			}
		}

		int ret = 0;
		b64 = new StringBuilder(b64).reverse().toString();

		int magnitude = 1;
		for (char character : b64.toCharArray())
		{
			int i = indexOf(character);
			if (i<0)
			{
				throw new IllegalArgumentException("Invalid character(s) in string: " + character);
			}

			ret += i * magnitude;
			//magnitude *= 64;
			magnitude *= C;
		}

		if (negate)
		{
			return -ret;
		}
		else
		{
			return ret;
		}
	}

	private
	Integer indexOf(char character)
	{
		//TODO: OPTIMIZE this linear search.
		for (int i=0; i<C; i++)
		{
			if (character == CHARACTERS[i])
			{
				return i;
			}
		}

		System.err.println("INVALID CHARACTER: "+character);
		return null;
	}
}

