package com.github.osndok.spr.squashball.impl;

import com.github.osndok.spr.Spr1Key;
import com.github.osndok.spr.Spr1Repo;
import com.github.osndok.spr.Spr1Tuple;
import com.github.osndok.spr.squashball.SquashReader;

import java.io.IOException;

public
class DeduplicatingSpr1RepoFilter
        implements Spr1Repo
{
    private final SquashReader blackList;
    private final Spr1Repo deeperStorage;

    public
    DeduplicatingSpr1RepoFilter(SquashReader blackList, Spr1Repo deeperStorage)
    {

        this.blackList = blackList;
        this.deeperStorage = deeperStorage;
    }

    @Override
    public
    void put(Spr1Tuple input) throws IOException
    {
        if (!blackList.seemsToContain(input))
        {
            deeperStorage.put(input);
        }
    }

    @Override
    public
    byte[] get(Spr1Key key) throws IOException
    {
        if (blackList.seemsToContain(key.getPublicBytes()))
        {
            return blackList.get(key);
        }
        else
        {
            return deeperStorage.get(key);
        }
    }

    @Override
    public
    boolean seemsToContain(byte[] sha1PublicHash) throws IOException
    {
        return blackList.seemsToContain(sha1PublicHash)
                || deeperStorage.seemsToContain(sha1PublicHash);
    }

    @Override
    public
    boolean seemsToContain(Spr1Tuple spr1Tuple) throws IOException
    {
        return seemsToContain(spr1Tuple.publicHash);
    }
}
