package com.allogy.spr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by robert on 2015-10-25 17:40.
 */
public
class Spr1Get implements Callable<File>
{
	private final
	Sha1Repo sha1Repo;

	private final
	Spr1Key spr1Key;

	private
	File destination;

	public
	Spr1Get(File repoDir, String spr1Key) throws IOException
	{
		this.sha1Repo = new Sha1Repo(repoDir);
		this.spr1Key = new Spr1Key(spr1Key);
	}

	public
	Spr1Get(File repoDir, String spr1Key, File destination) throws IOException
	{
		this.sha1Repo = new Sha1Repo(repoDir);
		this.spr1Key = new Spr1Key(spr1Key);
		this.destination = destination;
	}

	public
	File call() throws Exception
	{
		final
		File encryptedFile = sha1Repo.get(spr1Key.getPublicBytes());

		if (encryptedFile == null)
		{
			return null;
		}

		if (destination == null)
		{
			destination = new File("/tmp/spr1get-" + Thread.currentThread().hashCode());
		}

		final
		Spr1Encryption.StreamResult streamResult;
		{
			final
			FileInputStream fis = new FileInputStream(encryptedFile);

			final
			FileOutputStream fos = new FileOutputStream(destination);

			streamResult = new Spr1Encryption(spr1Key).decrypt(fis, fos);
		}

		assert (Arrays.equals(spr1Key.getPublicBytes(), streamResult.getPreHash()));
		assert (Arrays.equals(spr1Key.getPrivateBytes(), streamResult.getPostHash()));

		return destination;
	}
}

