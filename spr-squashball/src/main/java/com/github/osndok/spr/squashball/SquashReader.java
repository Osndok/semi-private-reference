package com.github.osndok.spr.squashball;

import com.github.osndok.spr.Spr1Repo;
import com.github.osndok.spr.Spr1Tuple;
import com.github.osndok.spr.squashball.impl.TableOfContents;

import java.io.IOException;

public
interface SquashReader extends Spr1Repo
{
    Spr1Tuple fetchPath(String path) throws IOException;

    TableOfContents getTableOfContents();
}
