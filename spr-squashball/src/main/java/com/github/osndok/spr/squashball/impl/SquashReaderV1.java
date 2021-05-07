package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Sha1Repo;
import com.github.osndok.spr.Spr1Key;
import com.github.osndok.spr.Spr1Tuple;
import com.github.osndok.spr.squashball.SquashReader;
import org.apache.hadoop.squashfs.MappedSquashFsReader;
import org.apache.hadoop.squashfs.SquashFsReader;
import org.apache.hadoop.squashfs.io.MappedFile;

import java.io.*;

public
class SquashReaderV1 implements SquashReader
{
    private static final boolean AVOID_MMAP = Boolean.getBoolean("SquashReader.NoMMAP");

    private final
    SquashFsReader squashFile;

    private
    TableOfContents tableOfContents;

    public
    SquashReaderV1(String filename) throws IOException
    {
        squashFile = openSquashFile(filename);
    }

    /**
     * https://github.com/ccondit-target/squashfs-tools/blob/master/src/main/java/org/apache/hadoop/squashfs/tools/SquashFsck.java#L54
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private static
    SquashFsReader openSquashFile(final String file) throws IOException
    {
        if (AVOID_MMAP)
        {
            return SquashFsReader.fromFile(0, new File(file));
        }

        try (var raf = new RandomAccessFile(file, "r"))
        {
            try (var channel = raf.getChannel())
            {
                var mmap = MappedFile.mmap(
                        channel,
                        MappedSquashFsReader.PREFERRED_MAP_SIZE,
                        MappedSquashFsReader.PREFERRED_WINDOW_SIZE
                );

                return SquashFsReader.fromMappedFile(0, mmap);
            }
        }
    }

    @Override
    public
    void put(final Spr1Tuple input) throws IOException
    {
        throw new UnsupportedEncodingException("SquashReader is read-only");
    }

    @Override
    public
    byte[] get(final Spr1Key key) throws IOException
    {
        return getTuple(key).clearTextBytes;
    }

    private
    Spr1Tuple getTuple(final Spr1Key key) throws IOException
    {
        var squashedPath = Sha1Repo.getRelativePath(key.getPublicBytes());
        var encrypted = getSquashedFile(squashedPath);
        return new Spr1Tuple(key, encrypted);
    }

    @Override
    public
    boolean seemsToContain(final byte[] sha1PublicHash) throws IOException
    {
        var squashedPath = Sha1Repo.getRelativePath(sha1PublicHash);
        try
        {
            var inode = squashFile.findInodeByPath(squashedPath);
            return true;
        }
        catch (FileNotFoundException e)
        {
            // TODO: optimize (using exception for expected path)
            return false;
        }
    }

    @Override
    public
    boolean seemsToContain(final Spr1Tuple spr1Tuple) throws IOException
    {
        return seemsToContain(spr1Tuple.publicHash);
    }

    @Override
    public
    Spr1Tuple fetchPath(final String path) throws IOException
    {
        if (tableOfContents == null)
        {
            throw new UnsupportedOperationException("toc must be decrypted to fetch files from a squashball");
        }

        var key = tableOfContents.get(path);
        return getTuple(key);
    }

    private
    byte[] getSquashedFile(final String squashedPath) throws IOException
    {
        var inode = squashFile.findInodeByPath(squashedPath);

        try (var baos = new ByteArrayOutputStream())
        {
            squashFile.writeFileStream(inode, baos);
            return baos.toByteArray();
        }
    }

    public
    void decryptTableOfContents(final String password) throws IOException
    {
        var challengeBytes = getSquashedFile(DecryptableChallengeV1.INDEX_FILE_NAME);
        var challenge = DecryptableChallengeV1.fromBytes(challengeBytes);
        var tocKey = challenge.recoverKey(password);
        var tocBytes = get(tocKey);
        tableOfContents = TableOfContents.fromBytes(tocBytes);
    }
}
