package com.github.osndok.spr;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by robert on 2015-10-26 00:23.
 */
@Test
public
class Spr1EncryptionTest extends Assert
{
	@BeforeClass
	public
	void setUp() throws Exception
	{
		Spr1Encryption.DEBUG=true;
	}

	/**
	 * These test vectors are from page 9 of Bernstein's work: "The Salsa20 family of stream ciphers"
	 */
	private static final
	byte[] EXAMPLE_KEY=new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};

	private static final
	byte[] EXAMPLE_NONCE=new byte[]{3, 1, 4, 1, 5, 9, 2, 6};

	private static final
	int EXAMPLE_BLOCK_NUMBER=7;

	//NB: The final output block is on page 11.
	private static final
	int[] EXPECTED_OUTPUT=new int[]
	{
		0xb9a205a3, 0x0695e150, 0xaa94881a, 0xadb7b12c,
		0x798942d4, 0x26107016, 0x64edb1a4, 0x2d27173f,
		0xb1c7f1fa, 0x62066edc, 0xe035fa23, 0xc4496f04,
		0x2131e6b3, 0x810bde28, 0xf62cb407, 0x6bdede3d,
	};

	/**
	 * From page 9 of Bernstein's work: "The Salsa20 family of stream ciphers"
	 */
	@Test
	public
	void testOriginallyPublishedVector()
	{
		final
		Spr1Encryption spr1Encryption = new Spr1Encryption(EXAMPLE_KEY, EXAMPLE_NONCE);

		final
		byte[] output = spr1Encryption.getSalsa20Block(EXAMPLE_BLOCK_NUMBER);

		littleEndianComparison(output, EXPECTED_OUTPUT);
	}

	private
	void littleEndianComparison(byte[] output, int[] expectedOutput)
	{
		System.err.println("--EXPECTED-- | --ACTUAL-- | (LITTLE ENDIAN! NOT BYTE ARRAYS!)");

		boolean success=true;
		int i=0;

		for (int expectedWord : expectedOutput)
		{
			System.err.print(String.format("  %08x   |  ", expectedWord));

			//FYI: little endian is most unpleasant and unnatural.
			int actualWord=little2big(intFromByteArray(output, i));

			i+=4;

			System.err.print(String.format("%08x", actualWord));

			if (actualWord==expectedWord)
			{
				System.err.println();
			}
			else
			{
				success=false;
				System.err.println(" <--- FAIL");
			}
		}

		assertTrue(success);
	}

	/*
	http://stackoverflow.com/questions/3842828/converting-little-endian-to-big-endian
	--
	Surly there is a standard java library function to do this?
	 */
	private
	int little2big(int i)
	{
		return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
	}

	/*
	http://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vise-versa
	--
	Again... surly there is a standard java library function for this, no?
	 */
	int intFromByteArray(byte[] bytes, int offset)
	{
		return bytes[offset] << 24 | (bytes[offset+1] & 0xFF) << 16 | (bytes[offset+2] & 0xFF) << 8 | (bytes[offset+3] & 0xFF);
	}

	/*
	These test vectors are from ECRYPT (NOTE: only use those with 256 bit keys!!!):
	http://www.ecrypt.eu.org/stream/svn/viewcvs.cgi/ecrypt/trunk/submissions/salsa20/full/verified.test-vectors?logsort=rev&rev=210&view=markup
	 */
	@Test
	public
	void testEcryptSalsa20_256_64_set1_vector0() throws Exception
	{
		byte[] key = hex(
			"80000000000000000000000000000000"+
			"00000000000000000000000000000000"
		);

		byte[] IV = hex(
			"0000000000000000"
		);

		final
		Spr1Encryption spr1Encryption=new Spr1Encryption(key, IV);

		byte[] block0 = hex(
			"E3BE8FDD8BECA2E3EA8EF9475B29A6E7"+
			"003951E1097A5C38D23B7A5FAD9F6844"+
			"B22C97559E2723C7CBBD3FE4FC8D9A07"+
			"44652A83E72A9C461876AF4D7EF1A117"
		);

		assertEquals(spr1Encryption.getSalsa20Block(0), block0);

		byte[] block3 = hex(
			"57BE81F47B17D9AE7C4FF15429A73E10"+
			"ACF250ED3A90A93C711308A74C6216A9"+
			"ED84CD126DA7F28E8ABF8BB63517E1CA"+
			"98E712F4FB2E1A6AED9FDC73291FAA17"
		);

		assertEquals(spr1Encryption.getSalsa20Block(3), block3);

		byte[] block4 = hex(
			"958211C4BA2EBD5838C635EDB81F513A"+
			"91A294E194F1C039AEEC657DCE40AA7E"+
			"7C0AF57CACEFA40C9F14B71A4B3456A6"+
			"3E162EC7D8D10B8FFB1810D71001B618"
		);

		assertEquals(spr1Encryption.getSalsa20Block(4), block4);

		byte[] block7 = hex(
			"696AFCFD0CDDCC83C7E77F11A649D79A"+
			"CDC3354E9635FF137E929933A0BD6F53"+
			"77EFA105A3A4266B7C0D089D08F1E855"+
			"CC32B15B93784A36E56A76CC64BC8477"
		);

		assertEquals(spr1Encryption.getSalsa20Block(7), block7);

		//xor-digest = 50EC2485637DB19C6E795E9C739382806F6DB320FE3D0444D56707D7B456457F3DB3E8D7065AF375A225A70951C8AB744EC4D595E85225F08E2BC03FE1C42567
	}

	private
	byte[] hex(String s) throws DecoderException
	{
		return Hex.decodeHex(s.toCharArray());
	}

	@Test
	public
	void testSingleBlockCryptoMatchesSalsa20Spec() throws Exception
	{
		//This is ECRYPT set 1 vector 9
		byte[] key=hex(
			"00400000000000000000000000000000"+
			"00000000000000000000000000000000"
		);

		byte[] IV = hex("0000000000000000");

		byte[] block0 = hex(
			"01F191C3A1F2CC6EBED78095A05E062E"+
			"1228154AF6BAE80A0E1A61DF2AE15FBC"+
			"C37286440F66780761413F23B0C2C9E4"+
			"678C628C5E7FB48C6EC1D82D47117D9F"
		);

		final
		Spr1Encryption spr1Encryption=new Spr1Encryption(key, IV);

		byte[] zeroInput=new byte[64];

		byte[] output=spr1Encryption.encrypt(zeroInput);
		{
			assertEquals(output, block0);
		}

		output=spr1Encryption.decrypt(zeroInput);
		{
			assertEquals(output, block0);
		}
	}

	@Test
	public
	void testMultiBlockCryptoMatchesSalsa20Spec() throws Exception
	{
		//This is ECRYPT vector 18
		final
		Spr1Encryption spr1Encryption;
		{
			final
			byte[] key = hex(
				"00002000000000000000000000000000" +
				"00000000000000000000000000000000"
			);

			final
			byte[] IV = hex(
				"0000000000000000"
			);

			spr1Encryption=new Spr1Encryption(key, IV);
		}

		final
		byte[] block3_bytes192_to_255 = hex(
			"77DE29C19136852CC5DF78B5903CAC7B"+
			"8C91345350CF97529D90F18055ECB75A"+
			"C86A922B2BD3BD1DE3E2FB6DF9153166"+
			"09BDBAB298B37EA0C5ECD917788E2216"
		);

		final
		byte[] block4_bytes256_to_319 = hex(
			"1985A31AA8484383B885418C78210D0E"+
			"84CBC7070A2ED22DCAAC6A739EAD5881"+
			"8E5F7755BE3BF0723A27DC69612F18DC"+
			"8BF9709077D22B78A365CE6131744651"
		);

		//First... if we encrypt ONE-BIG-ARRAY-OF-ZEROS all at once... do the test patterns match?
		final
		byte[] fiveBlocksOfZeros=new byte[64*5];
		{
			final
			byte[] output=spr1Encryption.decrypt(fiveBlocksOfZeros);

			final
			byte[] block3;
			{
				block3=Arrays.copyOfRange(output, 192, 255+1);
				assertEquals(block3, block3_bytes192_to_255);
			}

			final
			byte[] block4;
			{
				block4=Arrays.copyOfRange(output, 256, 319+1);
				assertEquals(block4, block4_bytes256_to_319);
			}
		}

		//Second... if we encrypt a stream just a few odd bytes at a time... do the test patterns match?
		final
		ByteArrayInputStream bais=new ByteArrayInputStream(fiveBlocksOfZeros);
		{
			//Set the buffer size to something that is not a power-of-two... to check the overlapping byte address logic.
			spr1Encryption.setBufferSize(7);

			final
			byte[] output;
			{
				final
				ByteArrayOutputStream baos=new ByteArrayOutputStream();

				spr1Encryption.encrypt(bais, baos);

				output=baos.toByteArray();
			}

			final
			byte[] block3;
			{
				block3=Arrays.copyOfRange(output, 192, 255+1);
				assertEquals(block3, block3_bytes192_to_255);
			}

			final
			byte[] block4;
			{
				block4=Arrays.copyOfRange(output, 256, 319+1);
				assertEquals(block4, block4_bytes256_to_319);
			}
		}
	}

}