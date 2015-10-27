package com.allogy.spr;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by robert on 2015-10-27 13:55.
 */
@Test
public
class EndToEndTest extends Assert
{
	private final
	File tempDir = new File("/tmp/sha1repo-test-" + this.hashCode());

	private
	Sha1Repo sha1Repo;

	@BeforeClass
	public
	void setUp() throws Exception
	{
		//super.setUp();

		tempDir.mkdir();

		sha1Repo = new Sha1Repo(tempDir);
	}

	@AfterClass
	public
	void tearDown() throws Exception
	{
		Recursively.deleteEntireDirectory(tempDir);
	}

	@Test
	public
	void testPutAndGetHello() throws Exception
	{
		final
		Spr1Put spr1Put=new Spr1Put(sha1Repo);
		{
			spr1Put.setBytes("hello".getBytes("UTF-8"));
		}

		final
		Spr1Key spr1Key = spr1Put.call();

		final
		String spr1KeyString = spr1Key.toString();
		{
			assertEquals(spr1KeyString, "spr1-"+"OAJe0eqWMDSFIOTrSXyPLjQTxVw"+"qvTGHdzF6KLavt4PO0gs2a6pQ00");
		}

		final
		byte[] expectedPrivateKey = Base64.decodeBase64("qvTGHdzF6KLavt4PO0gs2a6pQ00");
		{
			assertEquals(spr1Key.getPrivateBytes(), expectedPrivateKey);
		}

		final
		byte[] expectedPublicKey = Base64.decodeBase64("OAJe0eqWMDSFIOTrSXyPLjQTxVw");
		{
			assertEquals(spr1Key.getPublicBytes(), expectedPublicKey);
			assertTrue(sha1Repo.get(expectedPublicKey).exists());
		}

		final
		byte[] expectedPublicKey2=Hex.decodeHex("38025ed1ea9630348520e4eb497c8f2e3413c55c".toCharArray());
		{
			assertEquals(expectedPublicKey, expectedPublicKey2);
			assertEquals(spr1Key.getPublicSha1(), "sha1-"+"38025ed1ea9630348520e4eb497c8f2e3413c55c");
		}

		//(2) Now get it back...
		final
		Spr1Get spr1Get=new Spr1Get(sha1Repo, spr1Key);

		final
		File out=(File)spr1Get.call();

		assertTrue(out.exists());

		final
		String recoveredContent=readContentsOfFileAsString(out);

		assertEquals(recoveredContent, "hello");
	}

	private
	String readContentsOfFileAsString(File file) throws FileNotFoundException
	{
		return new Scanner(file).useDelimiter("\\Z").next();
	}
}
