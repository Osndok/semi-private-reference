package com.github.osndok.spr;

import java.io.IOException;

public
interface Spr1Repo
{
    void put(Spr1Tuple input) throws IOException;
    byte[] get(Spr1Key key) throws IOException;
    boolean seemsToContain(byte[] sha1PublicHash) throws IOException;
    boolean seemsToContain(Spr1Tuple spr1Tuple) throws IOException;
}
