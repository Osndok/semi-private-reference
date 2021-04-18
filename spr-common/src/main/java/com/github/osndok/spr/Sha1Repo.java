package com.github.osndok.spr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by robert on 2015-10-24 23:54.
 */
//@javax.module.CommandLineTool(name = "sha1repo", description = "basic/local sha1 content-addressed-storage proof-of-concept tool")
public
class Sha1Repo
{
	private final
	File dir;

	public
	Sha1Repo(File dir) throws IOException
	{
		this.dir = dir;

		if (!dir.isDirectory())
		{
			throw new IOException("not a directory: "+dir);
		}
	}

	private
	boolean gitCompatible;

	//@javax.module.CommandLineOption(_long = "git", _short = 'g')
	public
	void useGitFormat()
	{
		//TODO: add "git repo" support, which might just mean adding/kerning a "blob:" prefix when hashing.
		//gitCompatible=true;
		throw new UnsupportedOperationException("git support is unimplemented in this version");
	}

	/**
	 * Given a sha1 hash code (in byte form), return the name of the readable file. Or null if it does not
	 * appear in the repository.
	 *
	 * @param hash
	 * @return
	 * @throws IOException
	 */
	public
	File get(byte[] hash) throws IOException
	{
		MustLookLike.aSha1HashCode(hash);

		final
		File retval = getDestination(hash);

		System.err.println("GET: "+retval);

		if (retval.exists())
		{
			return retval;
		}
		else
		{
			System.err.println("WARN: dne: " + retval);
			return null;
		}
	}

	/**
	 * Given a sha1 hash code (in byte form), return the path that we would expect to find it, weither or not
	 * it actually exists.
	 *
	 * @param hash
	 * @return
	 * @throws IOException
	 */
	private
	File getDestination(byte[] hash) throws IOException
	{
		final
		File innerDir;
		{
			final
			String firstByte=String.format("%02x", hash[0]);

			innerDir=new File(dir, firstByte);
		}

		final
		String secondByte = String.format("%02x", hash[1]);

		final
		String remainingBytes = String.format(
			"%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x%02x",
			hash[ 2],hash[ 3],
			hash[ 4],hash[ 5],hash[ 6],hash[ 7],
			hash[ 8],hash[ 9],hash[10],hash[11],
			hash[12],hash[13],hash[14],hash[15],
			hash[16],hash[17],hash[18],hash[19]
		);

		final
		File retval;
		{
			if (gitCompatible)
			{
				retval=new File(innerDir, secondByte+remainingBytes);
			}
			else
			{
				retval=new File(new File(innerDir, secondByte), remainingBytes);
			}
		}

		return retval;
	}

	private
	int bufferSize=4096;

	public
	void setBufferSize(int bufferSize)
	{
		this.bufferSize=bufferSize;
	}

	/**
	 * Given a stream of data, store it into the repo while buffering only a small (configurable) portion of it at a time.
	 * The stream will be fully read and closed.
	 *
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public
	byte[] put(InputStream inputStream) throws IOException
	{
		final
		MessageDigest sha1=sha1Source.get();
		{
			sha1.reset();
		}

		final
		File tempFile=createTempFile();

		boolean success=false;

		final
		OutputStream outputStream=new FileOutputStream(tempFile);

		try
		{
			final
			byte[] buffer=new byte[bufferSize];

			int n;

			while ((n=inputStream.read(buffer))>0)
			{
				sha1.update(buffer, 0, n);
				outputStream.write(buffer, 0, n);
			}

			success=true;
		}
		finally
		{
			if (!success)
			{
				//Note order... in case close() throws an exception.
				tempFile.delete();
			}

			outputStream.close();
			inputStream.close();
		}

		final
		byte[] sha1Hash=sha1.digest();

		return absorb(tempFile, sha1Hash);
	}

	/**
	 * Given a small bit of data that wholly fits in memory, store it into the repository.
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public
	byte[] put(byte[] data) throws IOException
	{
		return put(data, 0, data.length);
	}

	/**
	 * Given a small bit of data that wholly fits in memory, store it into the repository.
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public
	byte[] put(byte[] data, int off, int len) throws IOException
	{
		final
		MessageDigest sha1=sha1Source.get();
		{
			sha1.reset();
		}

		final
		byte[] sha1Hash;
		{
			sha1.update(data, off, len);
			sha1Hash=sha1.digest();
		}

		final
		File destination=getDestination(sha1Hash);
		{
			if (destination.exists())
			{
				System.err.println("EXISTS: "+destination);
				return sha1Hash;
			}

			destination.getParentFile().mkdirs();
		}

		final
		File tempFile=new File(destination.getParent(), destination.getName()+".part."+sha1.hashCode());;

		boolean success=false;

		final
		OutputStream outputStream=new FileOutputStream(tempFile);

		try
		{
			outputStream.write(data, off, len);
			success=true;
		}
		finally
		{
			if (!success)
			{
				//Note order... in case close() throws an exception.
				tempFile.delete();
			}

			outputStream.close();
		}

		if (tempFile.renameTo(destination))
		{
			System.err.println("PUT: "+destination);
			return sha1Hash;
		}
		else
		{
			tempFile.delete();
			throw new IOException("unable to move-after-write: mv "+tempFile+" "+destination);
		}
	}

	private static final
	ThreadLocal<MessageDigest> sha1Source = new ThreadLocal<MessageDigest>()
	{
		@Override
		protected
		MessageDigest initialValue()
		{
			try
			{
				return MessageDigest.getInstance("SHA-1");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new AssertionError(e);
			}
		}
	};

	public static
	byte[] getSha1Sum(byte[] bytes)
	{
		final
		MessageDigest sha1=sha1Source.get();
		{
			sha1.reset();
		}

		return sha1.digest(bytes);
	}

	public static
	byte[] getSha1Sum(File file) throws IOException
	{
		final
		MessageDigest sha1 = sha1Source.get();
		{
			sha1.reset();
		}

		final
		FileInputStream in=new FileInputStream(file);

		try
		{
			final
			byte[] buffer = new byte[4096];

			int numBytes;

			while ((numBytes = in.read(buffer)) > 0)
			{
				sha1.update(buffer, 0, numBytes);
			}
		}
		finally
		{
			in.close();
		}

		return sha1.digest();
	}

	public
	File createTempFile()
	{
		return new File(dir, "DELETE_ME."+this.hashCode()+'-'+Thread.currentThread().hashCode());
	}

	public
	byte[] absorb(File tempFile, byte[] sha1Hash) throws IOException
	{
		final
		File destination=getDestination(sha1Hash);
		{
			if (destination.exists())
			{
				System.err.println("EXISTS: "+destination);
				tempFile.delete();
				return sha1Hash;
			}

			destination.getParentFile().mkdirs();
		}

		if (tempFile.renameTo(destination))
		{
			return sha1Hash;
		}
		else
		{
			tempFile.delete();
			throw new IOException("unable to move-after-write: mv "+tempFile+" "+destination);
		}
	}
}
