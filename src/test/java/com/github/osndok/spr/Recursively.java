package com.github.osndok.spr;

import java.io.File;
import java.io.IOException;

/**
 * Created by robert on 2015-10-27 13:56.
 */
class Recursively
{
	public static
	void deleteEntireDirectory(File file) throws IOException
	{
		if (file.isDirectory())
		{
			System.err.println("DELETE: "+file);

			for (File child : notNull(file.listFiles()))
			{
				deleteEntireDirectory(child);
			}
		}

		System.err.println("DELETE: " + file);

		if (!file.delete())
		{
			throw new IOException("Failed to delete file: " + file);
		}
	}

	private static
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
}
