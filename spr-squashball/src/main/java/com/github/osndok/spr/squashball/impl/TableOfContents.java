package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Spr1Key;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public
class TableOfContents
{
    private static final int V1_NOISE_BYTES = 256;

    private static final byte[] V1_MAGIC = "SPR1-TOC-V1".getBytes(StandardCharsets.UTF_8);

    private final
    Map<String, Spr1Key> keysByRelativePath = new HashMap<>();

    private final
    Random random = new Random();

    public static
    TableOfContents fromBytes(final byte[] tocBytes) throws IOException
    {
        var retval = new TableOfContents();

        try (var in = new DataInputStream(new ByteArrayInputStream(tocBytes)))
        {
            readV1Noise(in);
            readAndVerifyV1Magic(in);

            int size = in.readInt();

            for (int i = 0; i<size; i++)
            {
                var key = in.readUTF();
                var value = Spr1Key.readFrom(in);
                retval.add(key, value);
            }

            readV1Noise(in);
        }

        return retval;
    }

    public
    byte[] toBytes() throws IOException
    {
        try (var baos = new ByteArrayOutputStream())
        {
            try (var out = new DataOutputStream(baos))
            {
                writeV1Noise(out);
                out.write(V1_MAGIC);

                out.writeInt(keysByRelativePath.size());
                for (Map.Entry<String, Spr1Key> entry : keysByRelativePath.entrySet())
                {
                    out.writeUTF(entry.getKey());
                    entry.getValue().writeTo(out);
                }

                writeV1Noise(out);
                return baos.toByteArray();
            }
        }
    }

    private
    void writeV1Noise(final OutputStream out) throws IOException
    {
        var bytes = new byte[V1_NOISE_BYTES];
        random.nextBytes(bytes);
        out.write(bytes);
    }

    private static
    void readV1Noise(final InputStream in) throws IOException
    {
        var bytes = new byte[V1_NOISE_BYTES];
        in.read(bytes);
    }

    private static
    void readAndVerifyV1Magic(final InputStream in) throws IOException
    {
        final int size = V1_MAGIC.length;
        var bytes = new byte[size];
        int i = in.read(bytes);
        if (i!=size)
        {
            throw new IOException();
        }
        if (Arrays.equals(bytes, V1_MAGIC))
        {
            throw new IOException("magic mismatch, incorrect password?");
        }
    }

    public
    void add(final String relativePath, final Spr1Key key)
    {
        keysByRelativePath.put(relativePath, key);
    }

    public
    Spr1Key get(final String path)
    {
        return keysByRelativePath.get(path);
    }
}
