package com.github.osndok.spr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public
class Spr1Directory implements Spr1Repo
{
    private
    Sha1Repo sha1Repo;

    public
    Spr1Directory(File directory) throws IOException
    {
        sha1Repo = new Sha1Repo(directory);
    }

    @Override
    public
    void put(Spr1Tuple input) throws IOException
    {
        sha1Repo.put(input.encryptedBytes);
    }

    @Override
    public
    byte[] get(Spr1Key key) throws IOException
    {
        var encryptedFile = sha1Repo.get(key.getPublicBytes());
        //TODO: verify the encrypted file hash?
        var encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
        var clearTextbytes = new Spr1Encryption(key.getPrivateBytes()).decrypt(encryptedBytes);
        return clearTextbytes;
    }

    @Override
    public
    boolean seemsToContain(byte[] sha1PublicHash) throws IOException
    {
        return sha1Repo.seemsToContain(sha1PublicHash);
    }

    @Override
    public
    boolean seemsToContain(Spr1Tuple spr1Tuple) throws IOException
    {
        return seemsToContain(spr1Tuple.publicHash);
    }
}
