package com.github.osndok.spr;

import junit.framework.TestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by robert on 2015-10-25 00:47.
 */
@Test
public
class Sha1RepoTest extends Assert
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

	private static final
	byte[] TEST_DATA="hello, world".getBytes();

	private static final
	byte[] EXPECTED_HASH_CODE = parseHexBinary("b7e23ec29af22b0b4e41da31e868d57226121c84");

	public static byte[] parseHexBinary(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
								  + Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	@Test
	public
	void testBasicOperation() throws Exception
	{
		final
		byte[] hash = sha1Repo.put(TEST_DATA);

		assertTrue(Arrays.equals(EXPECTED_HASH_CODE, hash));

		final
		File file=sha1Repo.get(hash);

		assertNotNull(file);
		assertTrue(file.canRead());
	}

	@Test
	public
	void testBufferredOperation() throws Exception
	{
		sha1Repo.setBufferSize(2);

		final
		InputStream inputStream=new ByteArrayInputStream(TEST_DATA);

		final
		byte[] hash = sha1Repo.put(inputStream);

		assertTrue(Arrays.equals(EXPECTED_HASH_CODE, hash));

		final
		File file=sha1Repo.get(hash);

		assertNotNull(file);
		assertTrue(file.canRead());
	}

}
