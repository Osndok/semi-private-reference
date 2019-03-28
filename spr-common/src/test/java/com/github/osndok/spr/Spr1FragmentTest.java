package com.github.osndok.spr;

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
	String SHA1_HEX ="b7e23ec29af22b0b4e41da31e868d57226121c84";

	private static final
	String SPR1_B64 ="t-I-wpryKwtOQdox6GjVciYSHIQ";

	private
	byte[] BYTES;

	@BeforeClass
	public
	void setUp() throws Exception
	{
		//super.setUp();

		BYTES = Hex.decodeHex(SHA1_HEX.toCharArray());
	}

	@Test
	public
	void testGetPublicString() throws Exception
	{
		Spr1Fragment s=new Spr1Fragment(BYTES);
		{
			assertEquals(s.getPublicString(), SPR1_B64);
		}

		s=new Spr1Fragment(SPR1_B64);
		{
			assertEquals(s.getPublicString(), SPR1_B64);
		}

		s=new Spr1Fragment("spr1-"+ SPR1_B64);
		{
			assertEquals(s.getPublicString(), SPR1_B64);
		}

		s=new Spr1Fragment(SPR1_B64 +"NOISE");
		{
			assertEquals(s.getPublicString(), SPR1_B64);
		}

		s=new Spr1Fragment("spr1-"+ SPR1_B64 +"NOISE");
		{
			assertEquals(s.getPublicString(), SPR1_B64);
		}
	}

	@Test
	public
	void testGetPublicBytes() throws Exception
	{
		Spr1Fragment s=new Spr1Fragment(SPR1_B64);
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment(SPR1_B64 +"NOISE");
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment("spr1-"+ SPR1_B64);
		{
			assertTrue(Arrays.equals(s.getPublicBytes(), BYTES));
		}

		s=new Spr1Fragment("spr1-"+ SPR1_B64 +"NOISE");
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
			assertEquals(spr1Fragment.getPublicSha1Hex(), SHA1_HEX);
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
			//assertTrue(s.startsWith("spr1-" + SPR1_B64));
			assertEquals(s, "sha1-"+SHA1_HEX);
		}
	}

	@Test
	public
	void testEquals() throws Exception
	{
		final
		Spr1Fragment a=new Spr1Fragment(BYTES);

		final
		Spr1Fragment b=new Spr1Fragment(SPR1_B64);

		final
		Spr1Fragment c=new Spr1Fragment("spr1-"+ SPR1_B64);

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
		int expectedHashCode= SPR1_B64.hashCode();

		Spr1Fragment s=new Spr1Fragment(BYTES);
		{
			assertEquals(s.hashCode(), expectedHashCode);
		}

		s=new Spr1Fragment(SPR1_B64);
		{
			assertEquals(s.hashCode(), expectedHashCode);
		}
	}

	private
	Spr1Fragment s()
	{
		return new Spr1Fragment();
	}

	@Test
	public
	void testByteStringConversion()
	{
		Spr1Fragment s;

		s=s();
		{
			s.setPublicBytes(BYTES);
			assertEquals(s.getPublicString(), SPR1_B64);
			assertEquals(s.getPublicSha1Hex(), SHA1_HEX);
		}

		s=s();
		{
			s.setPublicString(SPR1_B64);
			assertEquals(s.getPublicBytes(), BYTES);
			assertEquals(s.getPublicSha1Hex(), SHA1_HEX);
		}
	}

	//Using the "DVD" example...
	@Test
	public
	void testBasicSizeHint()
	{
		Spr1Fragment s;

		//long to string/bytes
		s=s();
		{
			s.setSizeHint(4688183296l);
			assertEquals(s.getSizeHintString(), "ng");
			assertEquals(s.getSizeHintBytes(), new byte[]{4, -91});
		}

		//string to long/bytes
		s=s();
		{
			s.setSizeHintString("ng");
			assertEquals(s.getSizeHint(), new Long(4716714510l));
			assertEquals(s.getSizeHintBytes(), new byte[]{4, -91});
		}

		//bytes to string/long
		s=s();
		{
			s.setSizeHintBytes(new byte[]{4, -91});
			assertEquals(s.getSizeHintString(), "ng");
			assertEquals(s.getSizeHint(), new Long(4716714510l));
		}
	}
}
