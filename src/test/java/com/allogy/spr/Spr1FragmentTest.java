package com.allogy.spr;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by robert on 2015-10-25 01:24.
 */
@Test
public
class Spr1FragmentTest extends Assert
{
	private static final
	String HEX="b7e23ec29af22b0b4e41da31e868d57226121c84";

	private static final
	String EXPECTED_BASE64="t-I-wpryKwtOQdox6GjVciYSHIQ";

	private
	byte[] BYTES;

	@BeforeClass
	public
	void setUp() throws Exception
	{
		//super.setUp();

		BYTES = Hex.decodeHex(HEX.toCharArray());
	}

	@Test
	public
	void testGetPublicString() throws Exception
	{
		Spr1Fragment s=new Spr1Fragment(BYTES);
		{
			assertEquals(s.getPublicString(), EXPECTED_BASE64);
		}

		s=new Spr1Fragment(EXPECTED_BASE64);
		{
			assertEquals(s.getPublicString(), EXPECTED_BASE64);
		}

		s=new Spr1Fragment("spr1-"+EXPECTED_BASE64);
		{
			assertEquals(s.getPublicString(), EXPECTED_BASE64);
		}

		s=new Spr1Fragment(EXPECTED_BASE64+"NOISE");
		{
			assertEquals(s.getPublicString(), EXPECTED_BASE64);
		}

		s=new Spr1Fragment("spr1-"+EXPECTED_BASE64+"NOISE");
		{
			assertEquals(s.getPublicString(), EXPECTED_BASE64);
		}
	}

	@Test
	public
	void testGetPublicBytes() throws Exception
	{
		Spr1Fragment s=new Spr1Fragment(EXPECTED_BASE64);
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment(EXPECTED_BASE64+"NOISE");
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment("spr1-"+EXPECTED_BASE64);
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment("spr1-"+EXPECTED_BASE64+"NOISE");
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment(BYTES);
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}
	}

	@Test
	public
	void testGetPublicSha1Hex() throws Exception
	{
		final
		Spr1Fragment spr1Fragment=new Spr1Fragment(BYTES);
		{
			assertEquals(spr1Fragment.getPublicSha1Hex(), HEX);
		}
	}

	@Test
	public
	void testToString() throws Exception
	{
		final
		Spr1Fragment spr1Fragment=new Spr1Fragment(BYTES);

		final
		String s=spr1Fragment.toString();
		{
			assertTrue(s.startsWith("spr1-" + EXPECTED_BASE64));
		}
	}

	@Test
	public
	void testEquals() throws Exception
	{
		final
		Spr1Fragment a=new Spr1Fragment(BYTES);

		final
		Spr1Fragment b=new Spr1Fragment(EXPECTED_BASE64);

		final
		Spr1Fragment c=new Spr1Fragment("spr1-"+EXPECTED_BASE64);

		assertEquals(a, a);
		assertEquals(a, b);
		assertEquals(a, c);
		assertEquals(b, a);
		assertEquals(b, b);
		assertEquals(b, c);
		assertEquals(c, a);
		assertEquals(c, b);
		assertEquals(c, c);
	}

	@Test
	public
	void testHashCode() throws Exception
	{
		final
		int expectedHashCode=EXPECTED_BASE64.hashCode();

		Spr1Fragment s=new Spr1Fragment(BYTES);
		{
			assertEquals(s.hashCode(), expectedHashCode);
		}

		s=new Spr1Fragment(EXPECTED_BASE64);
		{
			assertEquals(s.hashCode(), expectedHashCode);
		}
	}
}