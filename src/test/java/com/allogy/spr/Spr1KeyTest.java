package com.allogy.spr;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by robert on 2015-10-25 02:01.
 */
@Test
public
class Spr1KeyTest extends Assert
{
	private static final
	String PUBLIC_HEX="b7e23ec29af22b0b4e41da31e868d57226121c84";

	private static final
	String PUBLIC_B64="t-I-wpryKwtOQdox6GjVciYSHIQ";

	private
	byte[] PUBLIC_BYTES;

	private static final
	String PRIVATE_HEX="25ee3ff7d2fd2ff15cfe704ad0a593802b8a474a";

	private
	byte[] PRIVATE_BYTES;

	private static final
	String PRIVATE_B64="Je4_99L9L_Fc_nBK0KWTgCuKR0o";

	private
	String withPrefix;

	private
	String withoutPrefix;

	@BeforeClass
	public
	void setUp() throws Exception
	{
		//super.setUp();

		PUBLIC_BYTES = Hex.decodeHex(PUBLIC_HEX.toCharArray());
		PRIVATE_BYTES = Hex.decodeHex(PRIVATE_HEX.toCharArray());

		withPrefix="spr1-"+PUBLIC_B64+PRIVATE_B64;
		withoutPrefix=PUBLIC_B64+PRIVATE_B64;
	}

	@Test
	public
	void testGetPrivateString() throws Exception
	{
		Spr1Key s=new Spr1Key(PUBLIC_BYTES, PRIVATE_BYTES);
		{
			assertEquals(PRIVATE_B64, s.getPrivateString());
		}

		s=new Spr1Key(withPrefix);
		{
			assertEquals(PRIVATE_B64, s.getPrivateString());
		}

		s=new Spr1Key(withoutPrefix);
		{
			assertEquals(PRIVATE_B64, s.getPrivateString());
		}

		s=new Spr1Key(withPrefix+"NOISE");
		{
			assertEquals(PRIVATE_B64, s.getPrivateString());
		}

		s=new Spr1Key(withoutPrefix+"NOISE");
		{
			assertEquals(PRIVATE_B64, s.getPrivateString());
		}
	}

	@Test
	public
	void testGetPrivateBytes() throws Exception
	{
		Spr1Key s=new Spr1Key(PUBLIC_BYTES, PRIVATE_BYTES);
		{
			assertTrue(Arrays.equals(PRIVATE_BYTES, s.getPrivateBytes()));
		}

		s=new Spr1Key(withPrefix);
		{
			assertTrue(Arrays.equals(PRIVATE_BYTES, s.getPrivateBytes()));
		}

		s=new Spr1Key(withoutPrefix);
		{
			assertTrue(Arrays.equals(PRIVATE_BYTES, s.getPrivateBytes()));
		}

		s=new Spr1Key(withPrefix+"NOISE");
		{
			assertTrue(Arrays.equals(PRIVATE_BYTES, s.getPrivateBytes()));
		}

		s=new Spr1Key(withoutPrefix+"NOISE");
		{
			assertTrue(Arrays.equals(PRIVATE_BYTES, s.getPrivateBytes()));
		}
	}

	@Test
	public
	void testToString() throws Exception
	{
		Spr1Key s=new Spr1Key(PUBLIC_BYTES, PRIVATE_BYTES);
		{
			assertEquals(withPrefix, s.toString());
		}

		s=new Spr1Key(withPrefix);
		{
			assertEquals(withPrefix, s.toString());
		}

		s=new Spr1Key(withoutPrefix);
		{
			assertEquals(withPrefix, s.toString());
		}

		s=new Spr1Key(withPrefix+"NOISE");
		{
			assertEquals(withPrefix, s.toString());
		}

		s=new Spr1Key(withoutPrefix+"NOISE");
		{
			assertEquals(withPrefix, s.toString());
		}
	}
}