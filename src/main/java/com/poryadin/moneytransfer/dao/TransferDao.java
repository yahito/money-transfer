package com.poryadin.moneytransfer.dao;

import com.poryadin.moneytransfer.datastore.DataStore;
import com.poryadin.moneytransfer.datastore.IdGenerator;
import com.poryadin.moneytransfer.model.Transfer;

public class TransferDao {
    private final DataStore dataStore;
    private final IdGenerator idGenerator;

    public TransferDao(DataStore dataStore, IdGenerator idGenerator) {
        this.dataStore = dataStore;
        this.idGenerator = idGenerator;
    }

    public Transfer createTransfer(Transfer transfer) {
        return dataStore.add(transfer.withId(idGenerator.generateId(Transfer.class)));
    }

    public Transfer updateState(Transfer transfer) {
        return dataStore.update(transfer);
    }
}
