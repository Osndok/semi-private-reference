package com.github.osndok.spr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by robert on 2015-10-25 17:40.
 */
//@javax.module.CommandLineTool(name="spr1-get")
public
class Spr1Get implements Callable
{
	private final
	Sha1Repo sha1Repo;

	private final
	Spr1Key spr1Key;

	private
	OutputStream destination;

	public
	Spr1Get(File repoDir, String spr1Key) throws IOException
	{
		this.sha1Repo = new Sha1Repo(repoDir);
		this.spr1Key = new Spr1Key(spr1Key);
	}

	public
	Spr1Get(File repoDir, String spr1Key, OutputStream destination) throws IOException
	{
		this.sha1Repo = new Sha1Repo(repoDir);
		this.spr1Key = new Spr1Key(spr1Key);
		this.destination = destination;
	}

	public
	Spr1Get(Sha1Repo sha1Repo, Spr1Key spr1Key)
	{
		this.sha1Repo=sha1Repo;
		this.spr1Key=spr1Key;
	}

	private
	File outputFile;

	public
	File getOutputFile()
	{
		return outputFile;
	}

	public
	Object call() throws Exception
	{
		final
		File encryptedFile = sha1Repo.get(spr1Key.getPublicBytes());

		if (encryptedFile == null)
		{
			return null;
		}

		if (destination == null)
		{
			outputFile = new File("/tmp/spr1get-" + Thread.currentThread().hashCode());

			destination = new FileOutputStream(outputFile);
		}

		final
		Spr1Encryption.StreamResult streamResult;
		{
			final
			FileInputStream fis = new FileInputStream(encryptedFile);

			streamResult = new Spr1Encryption(spr1Key).decrypt(fis, destination);
		}

		/*
		When *decoding* the pre-crypto bytes equate to the 'public' hash, and the post-crypto bytes
		are the 'private' hash.
		 */
		if (!Arrays.equals(spr1Key.getPublicBytes(), streamResult.getPreCryptoHash()))
		{
			throw new IOException("file/data-stream from sha1Repo was corrupted in storage");
		}

		if (!Arrays.equals(spr1Key.getPrivateBytes(), streamResult.getPostCryptoHash()))
		{
			throw new IOException("file/data-stream did not decode correctly, probably a corrupted SPR1 reference");
		}

		if (outputFile==null)
		{
			return Void.class;
		}
		else
		{
			return outputFile;
		}
	}
}

