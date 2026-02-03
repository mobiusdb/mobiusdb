/**
 * %%
 * Copyright (C) 2012 Benjamin Tan
 * %%
 */
package com.github.tanbamboo.mobiusdb;

import com.github.tanbamboo.mobiusdb.melt.MeltEnvelope;
import com.github.tanbamboo.mobiusdb.melt.MeltStore;
import com.github.tanbamboo.mobiusdb.melt.TagFilter;
import com.github.tanbamboo.mobiusdb.melt.TimeRange;
import com.github.tanbamboo.mobiusdb.melt.WalWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author ben
 *
 */
public class MobiusDB {
    private final MeltStore meltStore;
    private final WalWriter walWriter;

    public MobiusDB(Path rootDirectory) throws IOException {
        this.meltStore = new MeltStore(rootDirectory);
        this.walWriter = new WalWriter(rootDirectory);
        recoverFromWal();
    }

    public void ingest(MeltEnvelope envelope) throws IOException {
        walWriter.append(envelope);
        meltStore.append(envelope);
        walWriter.clear();
    }

    public List<MeltEnvelope> query(TimeRange range, TagFilter filter) throws IOException {
        return meltStore.query(range, filter);
    }

    private void recoverFromWal() throws IOException {
        List<MeltEnvelope> pending = walWriter.recover();
        if (!pending.isEmpty()) {
            meltStore.appendRecovered(pending);
            walWriter.clear();
        }
    }
}
