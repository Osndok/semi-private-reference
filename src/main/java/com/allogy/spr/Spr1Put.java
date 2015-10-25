package com.allogy.spr;

import javax.module.CommandLineOption;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Created by robert on 2015-10-25 02:11.
 */
public
class Spr1Put implements Callable<String>
{
	private final
	Sha1Repo sha1Repo;

	public
	Spr1Put(File repoDir) throws IOException
	{
		sha1Repo = new Sha1Repo(repoDir);
	}

	private
	byte[] bytes;

	public
	void setBytes(byte[] bytes)
	{
		this.bytes=bytes;
	}

	public
	InputStream inputStream;

	@CommandLineOption(_long = "file", _short = 'f')
	public
	void setInputStream(InputStream inputStream)
	{
		this.inputStream=inputStream;
	}

	public
	String call() throws Exception
	{
		final
		byte[] privateHash;

		final
		byte[] publicHash;
		{
			if (bytes==null)
			{
				if (inputStream==null)
				{
					throw new Exception("no bytes on inputStream specified");
				}
				else
				{
					//hash the stream while writing it to a temp file
					//encrypt the file
					//hash the encrypted file
					//store encrypted bytes in sha1repo
					//return the spr1 reference
					//TODO: implement me
					privateHash=null;
					publicHash=null;
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

		return new Spr1Key(publicHash, privateHash).toString();
	}
}
