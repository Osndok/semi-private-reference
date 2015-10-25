package com.allogy.spr;

import com.allogy.spr.Sha1Repo;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by robert on 2015-10-25 00:47.
 */
public
class Sha1RepoTest extends TestCase
{
	private final
	File tempDir = new File("/tmp/sha1repo-test-" + this.hashCode());

	private
	Sha1Repo sha1Repo;

	public
	void setUp() throws Exception
	{
		super.setUp();

		tempDir.mkdir();

		sha1Repo = new Sha1Repo(tempDir);
	}

	public
	void tearDown() throws Exception
	{
		recursivelyDeleteEntireDirectory(tempDir);
	}

	private
	void recursivelyDeleteEntireDirectory(File file) throws IOException
	{
		if (file.isDirectory())
		{
			System.err.println("DELETE: "+file);

			for (File child : notNull(file.listFiles()))
			{
				recursivelyDeleteEntireDirectory(child);
			}
		}

		System.err.println("DELETE: " + file);

		if (!file.delete())
		{
			throw new IOException("Failed to delete file: " + file);
		}
	}

	private
	File[] notNull(File[] files)
	{
		if (files==null)
		{
			return new File[0];
		}
		else
		{
			return files;
		}
	}

	private static final
	byte[] TEST_DATA="hello, world".getBytes();

	private static final
	byte[] EXPECTED_HASH_CODE=javax.xml.bind.DatatypeConverter.parseHexBinary("b7e23ec29af22b0b4e41da31e868d57226121c84");

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