package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Spr1Key;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public
class TableOfContents
{
    private static final int V1_NOISE_BYTES = 256;

    private final
    Map<String, Spr1Key> keysByRelativePath = new HashMap<>();

    private final
    Random random = new Random();

    public
    byte[] toBytes() throws IOException
    {
        //first thing to write should be some random bytes (or a UUID?), to comparing encrypted bits less effective.
        try (var baos = new ByteArrayOutputStream())
        {
            try (var out = new DataOutputStream(baos))
            {
                writeV1Noise(out);

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

    public
    void add(final String relativePath, final Spr1Key key)
    {
        keysByRelativePath.put(relativePath, key);
    }
}
