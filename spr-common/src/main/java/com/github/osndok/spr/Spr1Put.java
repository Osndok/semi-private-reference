package com.github.osndok.spr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by robert on 2015-10-25 02:11.
 */
//@javax.module.CommandLineTool(name="spr1-put")
public
class Spr1Put implements Callable<Spr1Key>
{
	private final
	Sha1Repo sha1Repo;

	public
	Spr1Put(File repoDir) throws IOException
	{
		if (!repoDir.exists())
		{
			repoDir.mkdir();
		}

		sha1Repo = new Sha1Repo(repoDir);
	}

	public
	Spr1Put(Sha1Repo sha1Repo)
	{
		this.sha1Repo=sha1Repo;
	}

	private
	byte[] bytes;

	public
	void setBytes(byte[] bytes)
	{
		this.bytes=bytes;
	}

	public
	File file;

	public
	void setFile(File file)
	{
		this.file = file;
	}

	public
	Spr1Key call() throws Exception
	{
		final
		byte[] privateHash;

		final
		byte[] publicHash;
		{
			if (bytes == null)
			{
				if (file == null)
				{
					throw new Exception("no bytes or file specified to put");
				}
				else
				{
					//hash the stream while writing it to a temp file
					//encrypt the file
					//hash the encrypted file
					//store encrypted bytes in sha1repo
					//return the spr1 reference
					privateHash = Sha1Repo.getSha1Sum(file);

					final
					File tempFile = sha1Repo.createTempFile();

					final
					Spr1Encryption.StreamResult streamResult;
					{
						final
						FileInputStream fis = new FileInputStream(file);

						final
						FileOutputStream fos = new FileOutputStream(tempFile);

						streamResult = new Spr1Encryption(privateHash).encrypt(fis, fos);
					}

					assert (Arrays.equals(privateHash, streamResult.getPreCryptoHash()));

					publicHash = streamResult.getPostCryptoHash();

					sha1Repo.absorb(tempFile, publicHash);
				}
			}
			else
			{
				privateHash=Sha1Repo.getSha1Sum(bytes);

				final
				byte[] encrypted=new Spr1Encryption(privateHash).encrypt(bytes);

				publicHash=sha1Repo.put(encrypted);
			}
		}

		return new Spr1Key(publicHash, privateHash);
	}
}
