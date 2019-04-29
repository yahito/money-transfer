package com.poryadin.moneytransfer.handlers;

import com.poryadin.moneytransfer.model.Result;
import com.poryadin.moneytransfer.model.Transfer;
import com.poryadin.moneytransfer.services.TransferService;

import java.util.List;
import java.util.Map;

public class TransferHandler extends JsonHandler<Transfer> {
    private final TransferService transferService;

    public TransferHandler(TransferService transferService) {
        super(Transfer.class);
        this.transferService = transferService;
    }

    @Override
    public Result handle(Map<String, List<String>> params, String body) {
        return transferService.transfer(convert(body).validate());
    }
}
