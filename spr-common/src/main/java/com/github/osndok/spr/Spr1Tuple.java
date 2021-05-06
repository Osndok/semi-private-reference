package com.github.osndok.spr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Intended for small files that easily fit into memory. This class will provide all the spr1 data & hashes
 * (given cleartext data) without the aid of an underlying repo.
 */
public
class Spr1Tuple
{
    public final
    byte[] clearTextBytes;

    public final
    byte[] privateHash;

    public final
    byte[] encryptedBytes;

    public final
    byte[] publicHash;

    public final
    Spr1Key spr1Key;

    public
    Spr1Tuple(File file) throws IOException
    {
        clearTextBytes = Files.readAllBytes(file.toPath());
        privateHash = Sha1Repo.getSha1Sum(clearTextBytes);
        encryptedBytes = new Spr1Encryption(privateHash).encrypt(clearTextBytes);
        publicHash = Sha1Repo.getSha1Sum(encryptedBytes);
        spr1Key = new Spr1Key(publicHash, privateHash);
    }

    public
    Spr1Tuple(byte[] clearBytes) throws IOException
    {
        clearTextBytes = clearBytes;
        privateHash = Sha1Repo.getSha1Sum(clearTextBytes);
        encryptedBytes = new Spr1Encryption(privateHash).encrypt(clearTextBytes);
        publicHash = Sha1Repo.getSha1Sum(encryptedBytes);
        spr1Key = new Spr1Key(publicHash, privateHash);
    }

    public
    Spr1Tuple(final Spr1Key key, final byte[] encrypted)
    {
        privateHash = key.getPrivateBytes();
        publicHash = key.getPublicBytes();
        spr1Key = key;
        encryptedBytes = encrypted;
        clearTextBytes = new Spr1Encryption(privateHash).decrypt(encryptedBytes);
    }
}
